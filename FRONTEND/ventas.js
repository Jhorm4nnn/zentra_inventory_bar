// Conecta ventas.html con la API REST de ZENTRA
const API = 'http://localhost:8080/api';

const formulario = document.querySelector('form');
const cuerpoTabla = document.querySelector('table tbody');
const campoProducto = document.getElementById('producto');
const campoCantidad = document.getElementById('cantidad');
const campoPrecio = document.getElementById('precio');
const campoTotal = document.getElementById('total');

// El precio lo pone el sistema según el producto; no se escribe a mano
campoPrecio.readOnly = true;

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

function formatearPrecio(valor) {
    return '$' + valor.toLocaleString('es-CO');
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

function buscarProducto(nombre) {
    const buscado = nombre.trim().toLowerCase();
    return productos.find(p => p.nombre.toLowerCase() === buscado);
}

// Pone el precio del producto elegido y calcula el total
function actualizarTotales() {
    const producto = buscarProducto(campoProducto.value);
    const cantidad = Number(campoCantidad.value);
    campoPrecio.value = producto ? producto.precio : '';
    campoTotal.value = producto && cantidad > 0 ? producto.precio * cantidad : '';
}

async function cargarProductos() {
    const respuesta = await fetch(`${API}/productos`);
    if (!respuesta.ok) {
        throw new Error(await leerError(respuesta));
    }
    productos = await respuesta.json();
    sugerencias.replaceChildren(...productos.map(p => {
        const opcion = document.createElement('option');
        opcion.value = p.nombre;
        opcion.label = `${formatearPrecio(p.precio)} · quedan ${p.stock}`;
        return opcion;
    }));
}

async function cargarRecientes() {
    const respuesta = await fetch(`${API}/ventas/recientes`);
    if (!respuesta.ok) {
        throw new Error(await leerError(respuesta));
    }
    const ventas = await respuesta.json();
    cuerpoTabla.replaceChildren();
    if (ventas.length === 0) {
        const fila = document.createElement('tr');
        const celda = crearCelda('Todavía no hay ventas registradas.');
        celda.colSpan = 3;
        fila.appendChild(celda);
        cuerpoTabla.appendChild(fila);
        return;
    }
    ventas.forEach(v => {
        const fila = document.createElement('tr');
        fila.appendChild(crearCelda(v.producto));
        fila.appendChild(crearCelda(v.cantidad));
        fila.appendChild(crearCelda(formatearPrecio(v.subtotal)));
        cuerpoTabla.appendChild(fila);
    });
}

async function cargarTodo() {
    try {
        await Promise.all([cargarProductos(), cargarRecientes()]);
    } catch (error) {
        cuerpoTabla.replaceChildren();
        mostrarMensaje('No se pudieron cargar los datos. ¿Está encendida la API?', true);
    }
}

async function registrarVenta(evento) {
    evento.preventDefault();

    const producto = buscarProducto(campoProducto.value);
    if (!producto) {
        mostrarMensaje('Ese producto no existe. Elígelo de la lista.', true);
        return;
    }

    try {
        const respuesta = await fetch(`${API}/ventas`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ idProducto: producto.id, cantidad: Number(campoCantidad.value) })
        });
        if (!respuesta.ok) {
            mostrarMensaje(await leerError(respuesta), true);
            return;
        }
        const venta = await respuesta.json();
        formulario.reset();
        mostrarMensaje(`Venta registrada. Total: ${formatearPrecio(venta.total)}.`);
        await cargarTodo();
    } catch (error) {
        mostrarMensaje('No se pudo conectar con la API. ¿Está encendida?', true);
    }
}

campoProducto.addEventListener('input', actualizarTotales);
campoCantidad.addEventListener('input', actualizarTotales);
formulario.addEventListener('submit', registrarVenta);

cargarTodo();
