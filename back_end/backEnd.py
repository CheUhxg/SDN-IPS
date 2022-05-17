from flask import Flask, render_template, request
from flask_cors import CORS, cross_origin
import basicShell


app = Flask(__name__)
app.config['CORS_HEADERS']='Content-Type'
cors = CORS(app)


@app.route('/basictopo', methods=['get', 'post'])

def getBasicTopo():
    
    return basicShell.basicTopoDisplay()


@app.route('/detectresult', methods = ['post'])
@cross_origin()
def result():
    
    return basicShell.getResult_and_count()


@app.route('/threatdisplay', methods = ['get', 'post'])
@cross_origin()
def display():

    return basicShell.threatcount


if __name__ == '__main__':
    app.run(host='192.168.0.114',port=5000)
