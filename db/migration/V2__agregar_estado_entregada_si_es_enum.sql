-- Solo es necesario si boletas.estado fue creado como un tipo ENUM nativo de PostgreSQL.
-- Es seguro volver a ejecutar: no modifica filas ni elimina valores.
DO $$
DECLARE
tipo_estado regtype;
  es_enum boolean;
BEGIN
SELECT a.atttypid::regtype, t.typtype = 'e'
INTO tipo_estado, es_enum
FROM pg_attribute a
         JOIN pg_class c ON c.oid = a.attrelid
         JOIN pg_namespace n ON n.oid = c.relnamespace
         JOIN pg_type t ON t.oid = a.atttypid
WHERE c.relname = 'boletas' AND n.nspname = current_schema()
  AND a.attname = 'estado' AND a.attnum > 0 AND NOT a.attisdropped;

IF es_enum THEN
    EXECUTE format('ALTER TYPE %s ADD VALUE IF NOT EXISTS %L', tipo_estado, 'ENTREGADA');
END IF;
END $$;
