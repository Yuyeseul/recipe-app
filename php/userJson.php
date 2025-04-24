<?php
$con = mysqli_connect("", "", "", "");
mysqli_query($con, 'SET NAMES utf8');

$query = "SELECT * FROM user";
$result = mysqli_query($con, $query);

if (!$result) {
    die('Query failed: ' . mysqli_error($con));
}
$response = array();
$i = 0;

while ($row = mysqli_fetch_array($result)) {
    $member = array();
    
    $member["id"] = $row["id"];
    $member["pw"] = $row["pw"];
    $member["name"] = $row["name"];
    $member["nickname"] = $row["nickname"];
    $member["birth"] = $row["birth"];
    $member["phone"] = $row["phone"];

    $response[$i] = $member;
    $i = $i + 1;
}

$json["user"] = $response;
$output = json_encode($json);
echo $output;

mysqli_close($con);
?>
