const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const app = express();

app.use(cors());
app.use(bodyParser.json({ limit: '50mb' })); // limit grande para el video en base64

let contactos = []; // base de datos en memoria
let nextId = 1;

// GET - Obtener todos los contactos
app.get('/contactos', (req, res) => {
    res.json(contactos);
});

// POST - Crear contacto
app.post('/contactos', (req, res) => {
    const nuevo = { id: String(nextId++), ...req.body };
    contactos.push(nuevo);
    res.status(201).json(nuevo);
});

// POST - Actualizar contacto (tu API usa POST con /{id})
app.post('/contactos/:id', (req, res) => {
    const idx = contactos.findIndex(c => c.id === req.params.id);
    if (idx === -1) return res.status(404).json({ error: 'No encontrado' });
    contactos[idx] = { ...contactos[idx], ...req.body };
    res.json(contactos[idx]);
});

// DELETE - Eliminar contacto
app.delete('/contactos/:id', (req, res) => {
    contactos = contactos.filter(c => c.id !== req.params.id);
    res.status(204).send();
});

app.listen(3000, '0.0.0.0', () => {
    console.log('Servidor corriendo en http://0.0.0.0:3000');
});