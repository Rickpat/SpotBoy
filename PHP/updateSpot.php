 <?php
 
 // array for JSON response
$response = array();
 

if( isset($_POST['id']) && isset($_POST['spotType']) ){
	$googleId = $_POST['id'];
	$spotType = $_POST['spotType'];
	// include db connect class
	require_once __DIR__ . '/spotboy_db_connect.php';
	// connecting to db
	$db = new DB_CONNECT();	
	$result = mysql_query("UPDATE spots SET spotType = '$spotType' WHERE id='$googleId'") or die(mysql_error());	
	// check if row inserted or not
    if ($result) {
        // successfully inserted into database
        $response["success"] = 1;
        $response["message"] = "Spot successfully updated."; 
        // echoing JSON response
        echo json_encode($response);
    } else {
        // failed to insert row
        $response["success"] = 2;
        $response["message"] = "spot update error";
        // echoing JSON response
        echo json_encode($response);
    }
} elseif( isset($_POST['id']) && isset($_POST['notes']) ){
	$googleId = $_POST['id'];
	$notes = $_POST['notes'];
	// include db connect class
	require_once __DIR__ . '/spotboy_db_connect.php';
	// connecting to db
	$db = new DB_CONNECT();	
	$result = mysql_query("UPDATE spots SET notes = '$notes' WHERE id='$googleId'") or die(mysql_error());
	// check if row inserted or not
    if ($result) {
        // successfully inserted into database
        $response["success"] = 1;
        $response["message"] = "Spot successfully updated.";
        // echoing JSON response
        echo json_encode($response);
    } else {
        // failed to insert row
		$response["action"] = "CREATE_SPOT";
        $response["success"] = 2;
        $response["message"] = "spot update error.";
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