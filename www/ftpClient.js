var exec = require('cordova/exec');

module.exports = {

    //String IPAddress1, String Port1, String UserName1, String Pass1, String Url1, String FileName1, String IndexRecord1	

    upload: function (IPAddress, Port, UserName, Pass, FolderUpload, Url, FileName, IndexRecord , success, failure) {
        return exec(success, failure, "FtpClient", "upload", [IPAddress, Port, UserName, Pass, FolderUpload, Url, FileName, IndexRecord]);
     },
     
     //downloadAsciiFile: function(file, url, success, failure) {
     //     return exec(success, failure, "FtpClient", "downloadAsciiFile", [file, url]);
     //},    
          
     downloadBinaryFile: function(ftpURL, nameFile, success, failure) {
         return exec(success, failure, "FtpClient", "downloadBinaryFile", [ftpURL, nameFile]);
     },
          
     //downloadAsciiString: function(url, success, failure) {
     //     return exec(success, failure, "FtpClient", "downloadAsciiString", ["", url]);
     //}
};
