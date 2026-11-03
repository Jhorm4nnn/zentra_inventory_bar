// Conecta productos.html con la API REST de ZENTRA
const API_URL = 'http://localhost:8080/api/productos';

const formulario = document.getElementById('form-producto');
const tituloFormulario = document.getElementById('titulo-formulario');
const botonGuardar = document.getElementById('boton-guardar');
const botonCancelar = document.getElementById('boton-cancelar');
const cuerpoTabla = document.getElementById('lista-productos');
const mensaje = document.getElementById('mensaje');

// Si tiene un número, el formulario está editando ese producto
let idEnEdicion = null;

function mostrarMensaje(texto, esError = false) {
    mensaje.textContent = texto;
    mensaje.classList.toggle('mensaje-error', esError);
}

function formatearPrecio(valor) {
    return '$' + valor.toLocaleString('es-CO');
}

// Lee el error que envía la API: {"campo": "mensaje"} o {"error": "mensaje"}
async function leerError(respuesta) {
    try {
        const datos = await respuesta.json();
        const textos = Object.values(datos);
        if (respuesta.status === 400 && !datos.status) {
            return textos.join('. ');
        }
    } catch (error) {
        // La respuesta no traía JSON: se usa el mensaje general
    }
    return 'La API respondió con el código ' + respuesta.status;
}

function crearBoton(texto, alHacerClic) {
    const boton = document.createElement('button');
    boton.type = 'button';
    boton.textContent = texto;
    boton.addEventListener('click', alHacerClic);
    return boton;
}

function crearCelda(texto) {
    const celda = document.createElement('td');
    celda.textContent = texto;
    return celda;
}

function mostrarProductos(productos) {
    cuerpoTabla.replaceChildren();

    if (productos.length === 0) {
        const fila = document.createElement('tr');
        const celda = crearCelda('Todavía no hay productos registrados.');
        celda.colSpan = 6;
        fila.appendChild(celda);
        cuerpoTabla.appendChild(fila);
        return;
    }

    productos.forEach(producto => {
        const fila = document.createElement('tr');
        fila.appendChild(crearCelda(producto.nombre));
        fila.appendChild(crearCelda(producto.categoria));
        fila.appendChild(crearCelda(formatearPrecio(producto.precio)));
        fila.appendChild(crearCelda(producto.stock));
        fila.appendChild(crearCelda(producto.stockMinimo));

        const acciones = document.createElement('td');
        acciones.appendChild(crearBoton('Actualizar', () => editarProducto(producto)));
        acciones.appendChild(document.createTextNode(' '));
        acciones.appendChild(crearBoton('Eliminar', () => eliminarProducto(producto)));
        fila.appendChild(acciones);

        cuerpoTabla.appendChild(fila);
    });
}

async function cargarProductos() {
    try {
        const respuesta = await fetch(API_URL);
        if (!respuesta.ok) {
            throw new Error(await leerError(respuesta));
        }
        mostrarProductos(await respuesta.json());
    } catch (error) {
        cuerpoTabla.replaceChildren();
        mostrarMensaje('No se pudieron cargar los productos. ¿Está encendida la API?', true);
    }
}

function editarProducto(producto) {
    idEnEdicion = producto.id;
    formulario.nombre.value = producto.nombre;
    formulario.categoria.value = producto.categoria;
    formulario.precio.value = producto.precio;
    formulario.stock.value = producto.stock;
    formulario.stockMinimo.value = producto.stockMinimo;
    tituloFormulario.textContent = 'Actualizar producto';
    botonGuardar.textContent = 'Guardar cambios';
    botonCancelar.hidden = false;
    mostrarMensaje('');
    formulario.nombre.focus();
}

function limpiarFormulario() {
    idEnEdicion = null;
    formulario.reset();
    tituloFormulario.textContent = 'Registrar producto';
    botonGuardar.textContent = 'Registrar producto';
    botonCancelar.hidden = true;
}

async function guardarProducto(evento) {
    evento.preventDefault();

    const producto = {
        nombre: formulario.nombre.value,
        categoria: formulario.categoria.value,
        precio: Number(formulario.precio.value),
        stock: Number(formulario.stock.value),
        stockMinimo: Number(formulario.stockMinimo.value)
    };

    const estaEditando = idEnEdicion !== null;
    const direccion = estaEditando ? `${API_URL}/${idEnEdicion}` : API_URL;

    try {
        const respuesta = await fetch(direccion, {
            method: estaEditando ? 'PUT' : 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(producto)
        });
        if (!respuesta.ok) {
            mostrarMensaje(await leerError(respuesta), true);
            return;
        }
        limpiarFormulario();
        mostrarMensaje(estaEditando ? 'Producto actualizado.' : 'Producto registrado.');
        await cargarProductos();
    } catch (error) {
        mostrarMensaje('No se pudo conectar con la API. ¿Está encendida?', true);
    }
}

async function eliminarProducto(producto) {
    if (!confirm(`¿Eliminar «${producto.nombre}»?`)) {
        return;
    }

    try {
        const respuesta = await fetch(`${API_URL}/${producto.id}`, { method: 'DELETE' });
        if (!respuesta.ok) {
            mostrarMensaje(await leerError(respuesta), true);
            return;
        }
        if (idEnEdicion === producto.id) {
            limpiarFormulario();
        }
        mostrarMensaje('Producto eliminado.');
        await cargarProductos();
    } catch (error) {
        mostrarMensaje('No se pudo conectar con la API. ¿Está encendida?', true);
    }
}

formulario.addEventListener('submit', guardarProducto);
botonCancelar.addEventListener('click', () => {
    limpiarFormulario();
    mostrarMensaje('');
});

cargarProductos();
