-- Script de migration pour passer de One-to-Many à Many-to-Many pour Task-Technicien

-- 1. Créer la table de liaison task_technicien
CREATE TABLE IF NOT EXISTS task_technicien (
    task_id BIGINT NOT NULL,
    technicien_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT, -- ID de l'utilisateur qui a fait l'assignation
    notes TEXT,
    PRIMARY KEY (task_id, technicien_id),
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (technicien_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by) REFERENCES users(id) ON DELETE SET NULL
);

-- 2. Migrer les données existantes de la colonne assigned_technicien_id
INSERT INTO task_technicien (task_id, technicien_id)
SELECT id, assigned_technicien_id
FROM tasks
WHERE assigned_technicien_id IS NOT NULL;

-- 3. Ajouter les nouvelles colonnes à la table tasks
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS progress_percentage INTEGER DEFAULT 0;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS progress_notes TEXT;

-- 4. Mettre à jour les tâches terminées avec completed_at
-- Utiliser created_at + un délai estimé ou la date actuelle pour les tâches déjà terminées
UPDATE tasks
SET completed_at = COALESCE(created_at + INTERVAL '7 days', CURRENT_TIMESTAMP)
WHERE status = 'COMPLETED' AND completed_at IS NULL;

-- 5. Supprimer l'ancienne colonne assigned_technicien_id (optionnel, à faire après tests)
-- ALTER TABLE tasks DROP COLUMN IF EXISTS assigned_technicien_id;

-- 6. Créer des index pour optimiser les performances
CREATE INDEX IF NOT EXISTS idx_task_technicien_task_id ON task_technicien(task_id);
CREATE INDEX IF NOT EXISTS idx_task_technicien_technicien_id ON task_technicien(technicien_id);
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);
CREATE INDEX IF NOT EXISTS idx_tasks_due_date ON tasks(due_date);
CREATE INDEX IF NOT EXISTS idx_tasks_progress ON tasks(progress_percentage);

-- 7. Créer une vue pour faciliter les requêtes sur les tâches avec techniciens
CREATE OR REPLACE VIEW v_tasks_with_techniciens AS
SELECT
    t.id as task_id,
    t.title,
    t.description,
    t.status,
    t.due_date,
    t.progress_percentage,
    t.created_at,
    t.completed_at,
    p.id as project_id,
    p.name as project_name,
    p.status as project_status,
    u.id as technicien_id,
    u.first_name || ' ' || u.last_name as technicien_name,
    u.skill_level,
    u.specialties,
    tt.assigned_at,
    tt.notes as assignment_notes
FROM tasks t
LEFT JOIN task_technicien tt ON t.id = tt.task_id
LEFT JOIN users u ON tt.technicien_id = u.id AND u.user_type = 'TECHNICIEN'
LEFT JOIN projects p ON t.project_id = p.id;

-- 8. Requêtes utiles pour vérifier la migration

-- Vérifier le nombre de tâches par technicien
SELECT
    u.first_name || ' ' || u.last_name as technicien_name,
    COUNT(tt.task_id) as total_tasks,
    COUNT(CASE WHEN t.status = 'TODO' THEN 1 END) as todo_tasks,
    COUNT(CASE WHEN t.status = 'IN_PROGRESS' THEN 1 END) as in_progress_tasks,
    COUNT(CASE WHEN t.status = 'REVIEW' THEN 1 END) as review_tasks,
    COUNT(CASE WHEN t.status = 'COMPLETED' THEN 1 END) as completed_tasks
FROM users u
LEFT JOIN task_technicien tt ON u.id = tt.technicien_id
LEFT JOIN tasks t ON tt.task_id = t.id
WHERE u.user_type = 'TECHNICIEN'
GROUP BY u.id, u.first_name, u.last_name
ORDER BY total_tasks DESC;

-- Vérifier les projets avec leurs statistiques
SELECT
    p.id,
    p.name as project_name,
    p.status as project_status,
    COUNT(t.id) as total_tasks,
    COUNT(CASE WHEN t.status = 'COMPLETED' THEN 1 END) as completed_tasks,
    ROUND(
        (COUNT(CASE WHEN t.status = 'COMPLETED' THEN 1 END) * 100.0) / NULLIF(COUNT(t.id), 0),
        2
    ) as progress_percentage,
    COUNT(DISTINCT tt.technicien_id) as assigned_techniciens_count
FROM projects p
LEFT JOIN tasks t ON p.id = t.project_id
LEFT JOIN task_technicien tt ON t.id = tt.task_id
GROUP BY p.id, p.name, p.status
ORDER BY progress_percentage DESC;

-- Vérifier les tâches non assignées
SELECT
    t.id,
    t.title,
    t.status,
    t.due_date,
    p.name as project_name
FROM tasks t
LEFT JOIN task_technicien tt ON t.id = tt.task_id
LEFT JOIN projects p ON t.project_id = p.id
WHERE tt.task_id IS NULL
ORDER BY t.due_date ASC;

-- Créer une fonction pour calculer automatiquement le pourcentage de progression d'un projet
CREATE OR REPLACE FUNCTION calculate_project_progress(project_id_param BIGINT)
RETURNS DECIMAL(5,2) AS $$
DECLARE
    total_tasks INTEGER;
    completed_tasks INTEGER;
    progress_percentage DECIMAL(5,2);
BEGIN
    SELECT
        COUNT(*),
        COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END)
    INTO total_tasks, completed_tasks
    FROM tasks
    WHERE project_id = project_id_param;

    IF total_tasks = 0 THEN
        RETURN 0.00;
    END IF;

    progress_percentage := (completed_tasks * 100.0) / total_tasks;
    RETURN ROUND(progress_percentage, 2);
END;
$$ LANGUAGE plpgsql;