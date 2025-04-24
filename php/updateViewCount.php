<?php
    $con = mysqli_connect("", "", "", "");
    
    mysqli_query($con, 'SET NAMES utf8');
    
    $recipeName = $_POST["recipeName"];
    $viewCount = $_POST["viewCount"];
    
    $query = "UPDATE recipe SET count = '$viewCount' WHERE recipeName = '$recipeName'";
    mysqli_query($con, $query);
    
    echo "View count updated successfully.";
    mysqli_close($con);
?>
