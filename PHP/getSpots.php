<?php
 
 
// array for JSON response
$response = array();
$result = array();
 
// include db connect class
require_once __DIR__ . '/spotboy_db_connect.php';

if(  /*isset($_POST['range']) && isset($_POST['spotType'])*/ true ){
	// connecting to db
	$db = new DB_CONNECT();
	$result = mysql_query("SELECT * FROM spots") or die(mysql_error());
	createResponse( $result );
}else{
	$response["success"] = 0;
    $response["message"] = "No spots found";
    echo json_encode($response);	
}

function createResponse( $result ){
	if (mysql_num_rows($result) > 0) {
		$response["spots"] = array();	 
		while ($row = mysql_fetch_array($result)) {
			$remote = array();
			$remote['id'] = $row['id'];
			$remote['googleId'] = $row['googleId'];
			$remote["geoPoint"] = $row['geoPoint'];
			$remote['spotType'] = $row['spotType'];
			$remote['notes'] = $row['notes'];
			$remote["imgURL"] = $row["imgURL"];
			$remote["creationTime"] = $row["creationTime"];
			array_push($response["spots"], $remote);
		}
		$response["success"] = 1;
		$response["message"] = "spots found";
		echo json_encode($response);
	}else{
		$response["success"] = 2;
		$response["message"] = "No spots found, All OK";
		echo json_encode($response);
	}
}



?>