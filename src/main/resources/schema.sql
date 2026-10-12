-- Precio y descripción históricos de PostgreSQL para las citas existentes.
-- Las nuevas columnas permiten nulos hasta completar las filas existentes.
-- La fecha de lectura es nula mientras la notificación permanezca pendiente.
ALTER TABLE notificacion ADD COLUMN IF NOT EXISTS fechalectura TIMESTAMP;

ALTER TABLE cita_servicio ADD COLUMN IF NOT EXISTS precio_aplicado NUMERIC(10, 2);
ALTER TABLE cita_servicio ADD COLUMN IF NOT EXISTS descripcion_aplicada TEXT;

UPDATE cita_servicio cs
SET precio_aplicado = s.precio,
    descripcion_aplicada = s.descripcion
FROM servicio s
WHERE s.id = cs.idservicio
  AND (cs.precio_aplicado IS NULL OR cs.descripcion_aplicada IS NULL);

-- SET NOT NULL verifica el backfill obligatorio del precio sin modificar los
-- datos de citas existentes. La descripción admite nulos porque Servicio también.
ALTER TABLE cita_servicio ALTER COLUMN precio_aplicado SET NOT NULL;
