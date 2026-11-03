-- Script de referencia de la base de datos de ZENTRA (MySQL).
-- No hace falta ejecutarlo: con ddl-auto=update, Hibernate crea la tabla
-- a partir de la clase Producto.

CREATE DATABASE IF NOT EXISTS zentradb;
USE zentradb;

CREATE TABLE IF NOT EXISTS producto (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    nombre       VARCHAR(255) NOT NULL,
    categoria    VARCHAR(255) NOT NULL,
    precio       DOUBLE       NOT NULL,
    stock        INT          NOT NULL,
    stock_minimo INT          NOT NULL,
    PRIMARY KEY (id)
);
