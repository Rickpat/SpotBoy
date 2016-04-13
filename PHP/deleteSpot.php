<?php
 
 
// array for JSON response
$response = array();
 
// Create User Request

if( isset($_POST['id'])){
	
	require_once __DIR__ . '/spotboy_db_connect.php';
	$db = new DB_CONNECT();	
	
	$id = (int) $_POST['id'];
	$deleteResult = false;
	$ftpResult = false;
	 
	$imgURL;
	$message = "no message";
	
	$dbResult = mysql_query("SELECT * FROM spots WHERE id = $id") or die(mysql_error());
	
	while ($row = mysql_fetch_array($dbResult)) {
		$imgURL = substr($row['imgURL'],27);
	}
	
	// connect and login to FTP server
	$ftp_server = "rickpat.bplaced.net";
	$ftp_username = "rickpat";
	$ftp_userpass = "hallowelt123";
	$ftp_conn = ftp_connect($ftp_server) or die("Could not connect to $ftp_server");
	$login = ftp_login($ftp_conn, $ftp_username, $ftp_userpass);
	// try to delete image file
	
	if($login){
		if (ftp_delete($ftp_conn, $imgURL)){
			$ftpResult = true;
			$message = "$imgURL deleted";
		  } else { 
			$message = "Could not delete $file";
		}
	}else{
		echo "login fail";
	}
	// close connection
	ftp_close($ftp_conn);
	
	// delete db entry
	if ($ftpResult){
		$deleteResult = mysql_query("DELETE FROM spots WHERE id ='$id'") or die(mysql_error());
		$message = $message." + db entry deleted";
	} else {
		$message = $message." + db entry not deleted";
	}
	
 
	// check if row inserted or not
	if ($deleteResult) {
		// successfully inserted into database
		$response["success"] = 1;
		$response["message"] = $message;
		// echoing JSON response
		echo json_encode($response);
	} else {
		// failed to insert row
		$response["success"] = 0;
		$response["message"] = $message;
 
		// echoing JSON response
		echo json_encode($response);
	}
} else {
    // required field is missing
    $response["success"] = 0;
    $response["message"] = "Required field(s) is missing";
    // echoing JSON response
    echo json_encode($response);
} 
?>