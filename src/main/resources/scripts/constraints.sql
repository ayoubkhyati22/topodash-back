ALTER TABLE users ALTER COLUMN created_by_topographe_id DROP NOT NULL;
ALTER TABLE users ALTER COLUMN assigned_to_topographe_id DROP NOT NULL;

-- Pour les clients uniquement
ALTER TABLE users ADD CONSTRAINT check_client_created_by
CHECK (
    (user_type = 'CLIENT' AND created_by_topographe_id IS NOT NULL)
    OR (user_type != 'CLIENT')
);

-- Pour les techniciens uniquement
ALTER TABLE users ADD CONSTRAINT check_technicien_assigned_to
CHECK (
    (user_type = 'TECHNICIEN' AND assigned_to_topographe_id IS NOT NULL)
    OR (user_type != 'TECHNICIEN')
);