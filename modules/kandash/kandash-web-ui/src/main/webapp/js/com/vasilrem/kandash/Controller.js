/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

const RESOURCES = "resources"
const RS_PROJECT = "/project"
const RS_TIER = "/tier"
const RS_TASK = "/task"
const RS_BOARD = "/board"
const RS_REPORTMODEL = "/reportmodel"

/**
 * Performs POST at given URL with passed parameters
 * @param url action URL
 * @param message this message will be POSTed
 */
function POST(url, message){
    return httpCall('POST', url, message)
}

/**
 * Performs PUT at given URL with passed parameters
 * @param url action URL
 * @param message this message will be PUT
 */
function PUT(url, message){
    return httpCall('PUT', url, message)
}

/**
 * Performs GET at given URL
 * @param url action URL
 */
function GET(url){
    return httpCall('GET', url)
}

/**
 * Performs DELETE at given URL
 * @param url action URL
 */
function DELETE(url){
    return httpCall('DELETE', url)
}

/**
 * Performs HTTP call
 * @param method HTTP method to be used
 * @param url actiob URL
 * @param message  parameters to be passed
 * @throw exception if HTTP call fails
 */
function httpCall(method, url, message){
    var result;
    if(message && !message._id){
        message._id = ""
    }
    Ext.Ajax.request({
        headers : {
            'X-HTTP-Method-Override' : method
        },
        method: method,
        async: false,
        url: url,
        params: Ext.encode(message),
        success: function(response) {
            result = response.responseText
        },
        failure: function(response){
            throw "Failed to " + method + " " +  url
        }
    });
    return result
}
