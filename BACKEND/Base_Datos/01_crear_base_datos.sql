-- =====================================================================
-- ZENTRA - Sistema de Gestión de Comandas e Inventario
-- 01: Base de datos y tablas (MySQL 8 o superior)
--
-- Basado en el diagrama de clases de la Entrega 3:
--   Usuario (RolUsuario) 1 ---- 0..* Venta 1 <>---- 1..* DetalleVenta
--   DetalleVenta 0..* ---- 1 Producto
--   Usuario 1 ---- 0..* Reporte (TipoReporte)
--
-- Se puede ejecutar varias veces: no borra datos existentes.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS zentra_bar
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_spanish_ci;

USE zentra_bar;

-- ---------------------------------------------------------------------
-- Usuario + RolUsuario (enum)
-- La contraseña se guarda cifrada con SHA-256, nunca en texto plano.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario (
    id_usuario  INT          NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(100) NOT NULL,
    usuario     VARCHAR(50)  NOT NULL,
    contrasena  CHAR(64)     NOT NULL COMMENT 'Hash SHA-256 de la contraseña',
    rol         ENUM('VENDEDOR', 'ADMINISTRADOR') NOT NULL DEFAULT 'VENDEDOR',
    PRIMARY KEY (id_usuario),
    CONSTRAINT uq_usuario_usuario UNIQUE (usuario)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Producto
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS producto (
    id_producto   INT           NOT NULL AUTO_INCREMENT,
    nombre        VARCHAR(100)  NOT NULL,
    categoria     VARCHAR(50)   NOT NULL,
    precio        DECIMAL(12,2) NOT NULL,
    stock         INT           NOT NULL DEFAULT 0,
    stock_minimo  INT           NOT NULL DEFAULT 0,
    PRIMARY KEY (id_producto),
    CONSTRAINT uq_producto_nombre       UNIQUE (nombre),
    CONSTRAINT ck_producto_precio       CHECK (precio > 0),
    CONSTRAINT ck_producto_stock        CHECK (stock >= 0),
    CONSTRAINT ck_producto_stock_minimo CHECK (stock_minimo >= 0)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Venta (un Usuario registra 0..* ventas)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS venta (
    id_venta    INT           NOT NULL AUTO_INCREMENT,
    fecha       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total       DECIMAL(12,2) NOT NULL DEFAULT 0,
    id_usuario  INT           NOT NULL,
    PRIMARY KEY (id_venta),
    INDEX ix_venta_fecha (fecha),
    CONSTRAINT ck_venta_total CHECK (total >= 0),
    CONSTRAINT fk_venta_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- DetalleVenta (composición: si se borra la venta, se borran sus detalles)
-- subtotal se calcula solo: cantidad * precio_unitario
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS detalle_venta (
    id_detalle_venta  INT           NOT NULL AUTO_INCREMENT,
    id_venta          INT           NOT NULL,
    id_producto       INT           NOT NULL,
    cantidad          INT           NOT NULL,
    precio_unitario   DECIMAL(12,2) NOT NULL,
    subtotal          DECIMAL(12,2) AS (cantidad * precio_unitario) STORED,
    PRIMARY KEY (id_detalle_venta),
    CONSTRAINT ck_detalle_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_detalle_precio   CHECK (precio_unitario > 0),
    CONSTRAINT fk_detalle_venta FOREIGN KEY (id_venta)
        REFERENCES venta (id_venta)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto)
        REFERENCES producto (id_producto)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Reporte + TipoReporte (enum). Un Usuario genera 0..* reportes.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reporte (
    id_reporte        INT      NOT NULL AUTO_INCREMENT,
    fecha_generacion  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo              ENUM('VENTAS_DEL_DIA', 'PRODUCTOS_BAJO_STOCK') NOT NULL,
    id_usuario        INT      NOT NULL,
    PRIMARY KEY (id_reporte),
    CONSTRAINT fk_reporte_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE = InnoDB;
