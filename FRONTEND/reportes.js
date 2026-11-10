// Conecta reportes.html con la API REST de ZENTRA
const API = 'http://localhost:8080/api/reportes';

// La página tiene dos tablas: ventas del día y productos con bajo stock
const [cuerpoVentas, cuerpoBajoStock] = document.querySelectorAll('table tbody');

// Mensaje de error en la primera sección (usa los estilos de #mensaje de style.css)
const mensaje = document.createElement('p');
mensaje.id = 'mensaje';
mensaje.setAttribute('role', 'status');
document.querySelector('main section').appendChild(mensaje);

function mostrarError(texto) {
    mensaje.textContent = texto;
    mensaje.classList.add('mensaje-error');
}

function formatearPrecio(valor) {
    return '$' + valor.toLocaleString('es-CO');
}

function formatearFecha(texto) {
    const [anio, mes, dia] = texto.split('-').map(Number);
    return new Date(anio, mes - 1, dia).toLocaleDateString('es-CO', { dateStyle: 'long' });
}

function crearFila(textos) {
    const fila = document.createElement('tr');
    textos.forEach(texto => {
        const celda = document.createElement('td');
        celda.textContent = texto;
        fila.appendChild(celda);
    });
    return fila;
}

function filaVacia(texto, columnas) {
    const fila = crearFila([texto]);
    fila.firstChild.colSpan = columnas;
    return fila;
}

async function pedir(ruta) {
    const respuesta = await fetch(`${API}/${ruta}`);
    if (!respuesta.ok) {
        throw new Error('La API respondió con el código ' + respuesta.status);
    }
    return respuesta.json();
}

async function cargarReportes() {
    try {
        const [ventas, bajoStock] = await Promise.all([pedir('ventas-del-dia'), pedir('bajo-stock')]);

        cuerpoVentas.replaceChildren(crearFila([
            formatearFecha(ventas.fecha),
            ventas.cantidadVentas,
            formatearPrecio(ventas.totalVendido)
        ]));

        cuerpoBajoStock.replaceChildren();
        if (bajoStock.length === 0) {
            cuerpoBajoStock.appendChild(filaVacia('Ningún producto tiene el stock bajo.', 3));
        } else {
            bajoStock.forEach(p => {
                cuerpoBajoStock.appendChild(crearFila([p.nombre, p.stock, p.stockMinimo]));
            });
        }
    } catch (error) {
        cuerpoVentas.replaceChildren();
        cuerpoBajoStock.replaceChildren();
        mostrarError('No se pudieron cargar los reportes. ¿Está encendida la API?');
    }
}

cargarReportes();
