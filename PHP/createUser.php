<?php
// array for JSON response
$response = array();
 
// Create User Request


	if(isset($_POST['username']) && isset($_POST['pw']) && isset($_POST['mail'])){
 
	$username = $_POST['username'];
	$pw = $_POST['pw'];
	$mail = $_POST['mail'];
	
	// include db connect class
    require_once __DIR__ . '/location_db_connect.php';
 
    // connecting to db
    $db = new DB_CONNECT();
	//Check 
	$result = mysql_query("INSERT INTO buddies(username, pw, mail) VALUES('$username','$pw','$mail')");
	//Link for activation
	$veryfym2 = "http://rickpat.bplaced.net/AndroidProject/SPOTMAP_V02/verify.php?username=".urlencode($username);
	
	if($result){
		$mailresult = mail($mail,"verify Mail", $veryfym2);
	}
 
    // check if row inserted or not
    if ($result && $mailresult) {
        // successfully inserted into database
        $response["success"] = 1;
        $response["message"] = "Buddy successfully created.";
 
        // echoing JSON response
        echo json_encode($response);
    } else {
        // failed to insert row
        $response["success"] = 0;
        $response["message"] = "Nickname and/or Mail not available.";
 
        // echoing JSON response
        echo json_encode($response);
    }
	}
?>