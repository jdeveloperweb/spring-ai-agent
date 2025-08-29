package com.company.agent.application;

import com.company.agent.domain.Task;
import com.company.agent.domain.TaskRepository;
import com.company.agent.infrastructure.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final AgentOrchestrator orchestrator;
    private final TenantContext tenantContext;

    @Transactional
    public Task createTask(String prompt, Task.Mode mode, UUID flowId) {
        String tenant = tenantContext.getCurrentTenant();

        Task task = Task.builder()
                .id(UUID.randomUUID())
                .prompt(prompt)
                .status(Task.Status.PENDING)
                .mode(mode)
                .tenant(tenant)
                .flowId(flowId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Task savedTask = taskRepository.save(task);

        log.info("Task {} criada para tenant {} com modo {}",
                savedTask.getId(), tenant, mode);

        return savedTask;
    }

    @Transactional
    public Task executeTask(UUID taskId) {
        String tenant = tenantContext.getCurrentTenant();

        Task task = taskRepository.findById(taskId)
                .filter(t -> tenant.equals(t.getTenant()))
                .orElseThrow(() -> new IllegalArgumentException("Task não encontrada: " + taskId));

        if (!task.canBeExecuted()) {
            throw new IllegalStateException("Task " + taskId + " não pode ser executada. Status atual: " + task.getStatus());
        }

        return orchestrator.run(task);
    }

    @Async
    @Transactional
    public CompletableFuture<Task> executeTaskAsync(UUID taskId) {
        try {
            Task result = executeTask(taskId);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("Erro na execução assíncrona da task {}: {}", taskId, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Transactional
    public Task createAndExecuteTask(String prompt, Task.Mode mode, UUID flowId, boolean sync) {
        Task task = createTask(prompt, mode, flowId);

        if (sync) {
            return executeTask(task.getId());
        } else {
            executeTaskAsync(task.getId());
            return task;
        }
    }

    @Transactional(readOnly = true)
    public Task getTask(UUID taskId) {
        String tenant = tenantContext.getCurrentTenant();

        return taskRepository.findById(taskId)
                .filter(t -> tenant.equals(t.getTenant()))
                .orElseThrow(() -> new IllegalArgumentException("Task não encontrada: " + taskId));
    }

    @Transactional(readOnly = true)
    public List<Task> listTasks() {
        String tenant = tenantContext.getCurrentTenant();
        return taskRepository.findByTenant(tenant);
    }

    @Transactional(readOnly = true)
    public List<Task> listTasksByStatus(Task.Status status) {
        String tenant = tenantContext.getCurrentTenant();
        return taskRepository.findByTenantAndStatus(tenant, status);
    }

    @Transactional(readOnly = true)
    public List<Task> listTasksByFlow(UUID flowId) {
        String tenant = tenantContext.getCurrentTenant();

        // Verificar se o flow pertence ao tenant (delegado para FlowService se necessário)
        List<Task> tasks = taskRepository.findByFlowId(flowId);

        // Filtrar por tenant para segurança
        return tasks.stream()
                .filter(t -> tenant.equals(t.getTenant()))
                .toList();
    }

    @Transactional
    public void cancelTask(UUID taskId) {
        String tenant = tenantContext.getCurrentTenant();

        Task task = taskRepository.findById(taskId)
                .filter(t -> tenant.equals(t.getTenant()))
                .orElseThrow(() -> new IllegalArgumentException("Task não encontrada: " + taskId));

        if (task.isCompleted()) {
            throw new IllegalStateException("Task " + taskId + " já foi finalizada");
        }

        task.cancel();
        taskRepository.save(task);

        log.info("Task {} cancelada pelo tenant {}", taskId, tenant);
    }
}
