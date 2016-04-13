 <?php
  // array for JSON response
$response = array();

 
 if( isset($_POST['image']) && isset($_POST['rowId']) ){
 
	$image = $_POST['image'];
	$id = $_POST['rowId'];
	 
	$path = "images/$id.png";
	 
	$actualpath = "http://rickpat.bplaced.net/SpotBoy/$path";
	 
	 
	require_once __DIR__ . '/spotboy_db_connect.php';
	$db = new DB_CONNECT();	
 
	$result = mysql_query("UPDATE spots SET imgURL='$actualpath' WHERE id='$id'") or die(mysql_error());
 
	if($result){
		file_put_contents($path,base64_decode($image));
		//creates a new file...
		// successfully inserted into database
		$response["action"] = "IMAGE_UPLOAD";
		$response["success"] = 1;
		$response["message"] = "image successfully stored.";
		$response['rowId'] = $id;
		// echoing JSON response
		echo json_encode($response);
	}
 
 } else {
    // required field is missing
	$response["action"] = "IMAGE_UPLOAD";
    $response["success"] = 0;
    $response["message"] = "Required field(s) is missing";
	$response['rowId'] = -1;
	
 
    // echoing JSON response
    echo json_encode($response);
}
?>