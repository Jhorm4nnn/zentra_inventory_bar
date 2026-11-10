// Conecta inventario.html con la API REST de ZENTRA
const API_URL = 'http://localhost:8080/api/productos';

const cuerpoTabla = document.querySelector('table tbody');
const formulario = document.querySelector('form');
const campoProducto = document.getElementById('producto');
const campoStock = document.getElementById('stock');

// Mensaje debajo del formulario (usa los estilos de #mensaje de style.css)
const mensaje = document.createElement('p');
mensaje.id = 'mensaje';
mensaje.setAttribute('role', 'status');
formulario.after(mensaje);

// Lista de sugerencias para el campo Producto
const sugerencias = document.createElement('datalist');
sugerencias.id = 'lista-nombres-productos';
document.body.appendChild(sugerencias);
campoProducto.setAttribute('list', sugerencias.id);
campoProducto.autocomplete = 'off';

let productos = [];

function mostrarMensaje(texto, esError = false) {
    mensaje.textContent = texto;
    mensaje.classList.toggle('mensaje-error', esError);
}

async function leerError(respuesta) {
    try {
        const datos = await respuesta.json();
        if (respuesta.status === 400 && !datos.status) {
            return Object.values(datos).join('. ');
        }
    } catch (error) {
        // La respuesta no traía JSON
    }
    return 'La API respondió con el código ' + respuesta.status;
}

function crearCelda(texto) {
    const celda = document.createElement('td');
    celda.textContent = texto;
    return celda;
}

function mostrarInventario() {
    cuerpoTabla.replaceChildren();
    if (productos.length === 0) {
        const fila = document.createElement('tr');
        const celda = crearCelda('Todavía no hay productos registrados.');
        celda.colSpan = 4;
        fila.appendChild(celda);
        cuerpoTabla.appendChild(fila);
        return;
    }
    productos.forEach(p => {
        const fila = document.createElement('tr');
        fila.appendChild(crearCelda(p.nombre));
        fila.appendChild(crearCelda(p.stock));
        fila.appendChild(crearCelda(p.stockMinimo));
        fila.appendChild(crearCelda(p.stock <= p.stockMinimo ? 'Bajo stock' : 'Disponible'));
        cuerpoTabla.appendChild(fila);
    });

    sugerencias.replaceChildren(...productos.map(p => {
        const opcion = document.createElement('option');
        opcion.value = p.nombre;
        return opcion;
    }));
}

async function cargarInventario() {
    try {
        const respuesta = await fetch(API_URL);
        if (!respuesta.ok) {
            throw new Error(await leerError(respuesta));
        }
        productos = await respuesta.json();
        mostrarInventario();
    } catch (error) {
        cuerpoTabla.replaceChildren();
        mostrarMensaje('No se pudo cargar el inventario. ¿Está encendida la API?', true);
    }
}

function buscarProducto(nombre) {
    const buscado = nombre.trim().toLowerCase();
    return productos.find(p => p.nombre.toLowerCase() === buscado);
}

async function actualizarStock(evento) {
    evento.preventDefault();

    const producto = buscarProducto(campoProducto.value);
    if (!producto) {
        mostrarMensaje('Ese producto no existe. Elígelo de la lista.', true);
        return;
    }
    const nuevoStock = Number(campoStock.value);

    try {
        const respuesta = await fetch(`${API_URL}/${producto.id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ ...producto, stock: nuevoStock })
        });
        if (!respuesta.ok) {
            mostrarMensaje(await leerError(respuesta), true);
            return;
        }
        formulario.reset();
        mostrarMensaje(`Stock de «${producto.nombre}» actualizado a ${nuevoStock}.`);
        await cargarInventario();
    } catch (error) {
        mostrarMensaje('No se pudo conectar con la API. ¿Está encendida?', true);
    }
}

formulario.addEventListener('submit', actualizarStock);

cargarInventario();
