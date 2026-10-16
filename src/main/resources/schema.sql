-- Agregar la fecha de lectura de las notificaciones.
ALTER TABLE notificacion
ADD COLUMN IF NOT EXISTS fechalectura TIMESTAMP;

-- Agregar las columnas para conservar el precio y la descripción históricos.
ALTER TABLE cita_servicio
ADD COLUMN IF NOT EXISTS precio_aplicado NUMERIC(10, 2);

ALTER TABLE cita_servicio
ADD COLUMN IF NOT EXISTS descripcion_aplicada TEXT;

-- Completar el precio únicamente si todavía no tiene un valor.
UPDATE cita_servicio cs
SET precio_aplicado = s.precio
FROM servicio s
WHERE s.id = cs.idservicio
  AND cs.precio_aplicado IS NULL;

-- Completar la descripción únicamente si todavía no tiene un valor.
UPDATE cita_servicio cs
SET descripcion_aplicada = s.descripcion
FROM servicio s
WHERE s.id = cs.idservicio
  AND cs.descripcion_aplicada IS NULL;

-- Verificar que todos los registros tengan un precio aplicado.
ALTER TABLE cita_servicio
ALTER COLUMN precio_aplicado SET NOT NULL;