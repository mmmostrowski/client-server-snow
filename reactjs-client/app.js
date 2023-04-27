const express = require('express')
const app = express()

app.use(express.static('build'))

app.all('*', function(req, res) {
    res.sendFile(__dirname + '/build/index.html');
});

app.listen(3000, () => console.log('Server is listening under: http://127.0.0.1:3000/'))
