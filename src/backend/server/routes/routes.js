const express = require('express');
const router = express.Router();
const server = require('../controllers/general');

//-----------------------GENERAL-------------------------
router.get('/home', server.viewHome);
router.post('/login', server.login , server.viewUsersHome);
router.post('/logout', server.logout , server.viewHome);

router.get('/getDataForCharts', server.getDataForCharts)

//-----------------------EDITING-------------------------
router.post('/editStatement', server.editStatements);
router.post('/updateStatement' , server.updateStatement, server.viewUsersHome);
module.exports = router;