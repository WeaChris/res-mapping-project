const { firestore } = require("firebase-admin");
const { app, db, auth } = require("../firebaseSync");

exports.viewHome = (req, res) => {
    res.render('home', { req });
}

exports.viewUsersHome = async (req, res) => {
    
    
    let arrayOfMarkers = await createMarkers();
    let arrayOfStatements = await statementFoldersReadyForShow();
    
    
    res.render('userHome', {arrayOfMarkers, arrayOfStatements});
}

exports.login = (req, res, next) => {
    const { email, password } = req.body;
    console.log(email, password);
    const current = auth.getAuth();

    auth.signInWithEmailAndPassword(current, email, password).then((user) => {
        console.log("success");
        next();
    }).catch((error) => {
        if (error.code === "auth/wrong-password") {
            console.log("ERROR");
        }
        console.log(error);
    })
}

exports.logout = (req, res, next) => {
    const current = auth.getAuth();
    auth.signOut(current);
    next();
}

exports.editStatements = async (req, res) => {
    const {id} = req.body;
    let statementFolder = await getStatementFoldersbyID(id);
    
    let statement = {
        "id" : statementFolder.id,
        "name" : statementFolder.data().name,
        "date" : statementFolder.data().date.toDate().toString(),
        "dateraw" : statementFolder.data().date, // for sorting purposes
        "location" : statementFolder.data().location._latitude + " , "+statementFolder.data().location._longitude,
        "power" : statementFolder.data().power + " kWp",
        "type" : statementFolder.data().type,
        "enviromental_pdf" : statementFolder.data().enviromental_pdf,
        "final" : statementFolder.data().final,
        "comment" : statementFolder.data().comment
    }

    res.render('viewEditStatement', {statement});
}

exports.updateStatement = async (req,res,next) =>{
    const {id, final ,comment,status} = req.body;
    let toBool = (final === 'true');
    
    const dict = {
        "final":toBool,
        "comment":comment,
        "status" : Number(status)
    }
    let statement = db.collection("statementFolders").doc(id);
    statement.update(dict);
    next();
}

exports.getDataForCharts = async (req, res) => {
    let statementFolders = await getStatementFolders();

    let readyArray = [];
    statementFolders.forEach((statementFolder) => {
        
        let statement = {
            "status" : statementFolder.data().status
        }
        
        readyArray.push(statement);
    });

    res.send(readyArray);
}

async function getStatementFoldersbyID(id) {
    return new Promise((resolve,reject) =>{

        console.log("ID ----------------------------- ", id);
        let statementFolder = db.collection("statementFolders").doc(id);
        statementFolder.get().then((querySnapshot) => {
            resolve(querySnapshot);
        }).catch((err)=>{
            reject(err);
        })
    })
    
}

async function getStatementFoldersByUsersCollection(){
    return new Promise((resolve,reject) => {
        let users = db.collection("Users");
        users.get().then((querySnapshot) => {
            querySnapshot.data().collection("statementFolders").get().then((querySnapshot2)=>{
                resolve(querySnapshot2);
            })
        })
     })
}

async function getStatementFolders() {
    return new Promise((resolve,reject) =>{
        let statementFolders = db.collection("statementFolders");
        statementFolders.get().then((querySnapshot) =>{
            
            resolve(querySnapshot);
        })
    })
    
}

async function statementFoldersReadyForShow(){
    let arrayOfStatementFolders = await getStatementFolders();
    let readyArray = [];
    arrayOfStatementFolders.forEach((statementFolder) => {
        
        let statement = {
            "id" : statementFolder.id,
            "name" : statementFolder.data().name,
            "date" : statementFolder.data().date.toDate().toString(),
            "dateraw" : statementFolder.data().date, // for sorting purposes
            "location" : statementFolder.data().location._latitude + " , "+statementFolder.data().location._longitude,
            "power" : statementFolder.data().power + " kWp",
            "type" : statementFolder.data().type,
            "enviromental_pdf" : statementFolder.data().enviromental_pdf
        }
        
        readyArray.push(statement);
    });

    return readyArray.sort((a, b) => {return a.dateraw-b.dateraw}).reverse(); // sorting before returning;
}

async function createMarkers(){
    let arrayOfStatementFolders = await getStatementFolders();
    let arrayOfMarkers = [];
    
    arrayOfStatementFolders.forEach((statementFolder) => {
        
        let marker = {
            "location" : statementFolder.data().location,
            "status" : statementFolder.data().status
        }
        
        arrayOfMarkers.push(marker);
    });
    
    return arrayOfMarkers;
}

async function addStatementFolders(){
    let statementFolder = {
        "date" : firestore.Timestamp.fromDate(new Date()),
        "enviromental_pdf" : "",
        "location" : new firestore.GeoPoint(37.983810, 23.727539),
        "name" : "test2",
        "power" : 1,
        "status" : 2,
        "type" : ""
    }

    db.collection("statementFolders").add(statementFolder);
}

