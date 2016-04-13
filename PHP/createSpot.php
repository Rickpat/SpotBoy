 <?php
 
 // array for JSON response
$response = array();
 

if( isset($_POST['googleId']) && isset($_POST['geoPoint']) && isset($_POST['spotType']) && isset($_POST['notes']) && isset($_POST['creationTime']) ){
	$googleId = $_POST['googleId'];
	$geoPoint = $_POST['geoPoint'];
	$spotType = $_POST['spotType'];
	$notes = $_POST['notes'];
	$creationTime = $_POST['creationTime'];

	// include db connect class
	require_once __DIR__ . '/spotboy_db_connect.php';

	// connecting to db
	$db = new DB_CONNECT();	

	$result = mysql_query("INSERT INTO spots(googleId,geoPoint,spotType,notes,creationTime) VALUES('$googleId','$geoPoint','$spotType','$notes','$creationTime')") or die(mysql_error());
	
	$rowId = mysql_insert_id();
	
	// check if row inserted or not
    if ($result) {
        // successfully inserted into database
		$response["action"] = "CREATE_SPOT";
        $response["success"] = 1;
        $response["message"] = "Spot successfully created.";
		$response["rowId"] = $rowId;
 
        // echoing JSON response
        echo json_encode($response);
    } else {
        // failed to insert row
		$response["action"] = "CREATE_SPOT";
        $response["success"] = 2;
        $response["message"] = "Oops! An error occurred.";
		$response["rowId"] = 2;
        // echoing JSON response
        echo json_encode($response);
    }
} else {
    // required field is missing
	$response["action"] = "CREATE_SPOT";
    $response["success"] = 0;
    $response["message"] = "Required field(s) is missing";
	$response["rowId"] = 0;
    // echoing JSON response
    echo json_encode($response);
}
?>