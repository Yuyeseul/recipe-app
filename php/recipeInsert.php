<?php
header('Content-Type: application/json');

$con = mysqli_connect("", "", "", "");
mysqli_query($con, 'SET NAMES utf8');

if (mysqli_connect_errno()) {
    die(json_encode(array("success" => false, "message" => "Database connection failed: " . mysqli_connect_error())));
}

$recipeName = isset($_POST["recipeName"]) ? mysqli_real_escape_string($con, $_POST["recipeName"]) : "";
$recipeCategory = isset($_POST["recipeCategory"]) ? mysqli_real_escape_string($con, $_POST["recipeCategory"]) : "";
$recipeIngredients = isset($_POST["recipeIngredients"]) ? mysqli_real_escape_string($con, $_POST["recipeIngredients"]) : "";
$recipeContent = isset($_POST["recipeContent"]) ? mysqli_real_escape_string($con, $_POST["recipeContent"]) : "";
$nickname = isset($_POST["nickname"]) ? mysqli_real_escape_string($con, $_POST["nickname"]) : "";

if (empty($nickname)) {
    echo json_encode(array("success" => false, "message" => "nickname cannot be empty."));
    exit();
}

$image_url = "";

if (isset($_FILES['image'])) {
    // 현재 시간을 가져옴 (형식: YYYYMMDD_HHMMSS)
    $dayTime = date('Ymd_His');

    $tmp_name = $_FILES['image']['tmp_name'];
    $targetDir = __DIR__ . "/image/";

    // 이미지 이름 설정 (예: 20241017_174526_0.jpg)
    $image_name = $dayTime . ".jpg"; // 파일 확장자는 필요에 따라 수정 가능
    $targetFile = $targetDir . $image_name;

    // 파일 형식 확인
    $allowedExtensions = ['jpg', 'jpeg', 'png', 'gif'];
    $fileExtension = pathinfo($_FILES['image']['name'], PATHINFO_EXTENSION);
    if (!in_array(strtolower($fileExtension), $allowedExtensions)) {
        echo json_encode(array("success" => false, "message" => "Invalid file type: " . $_FILES['image']['name']));
        exit();
    }

    if ($_FILES['image']['error'] != UPLOAD_ERR_OK) {
        error_log("File upload error for image: " . $_FILES['image']['name'] . " Error code: " . $_FILES['image']['error']);
        echo json_encode(array("success" => false, "message" => "File upload error for " . $_FILES['image']['name'] . ": " . $_FILES['image']['error']));
        exit();
    }

    if (move_uploaded_file($tmp_name, $targetFile)) {
        $image_url = $targetFile; // 하나의 이미지 URL만 저장
    } else {
        echo json_encode(array("success" => false, "message" => "Failed to upload file: " . $_FILES['image']['name']));
        exit();
    }
}

$recipeImage = $image_url; // 이미지 URL을 저장

$favorite = 0;
$count = 0;

$statement = mysqli_prepare($con, "INSERT INTO recipe (recipeName, recipeImage, recipeCategory, recipeIngredients, recipeContent, dayTime, nickname, favorite, count) VALUES (?, ?, ?, ?, ?, NOW(), ?, ?, ?)");

// 매개변수 바인딩
mysqli_stmt_bind_param($statement, "ssssssii", $recipeName, $recipeImage, $recipeCategory, $recipeIngredients, $recipeContent, $nickname, $favorite, $count);

// 쿼리 실행
$response = [];
if (mysqli_stmt_execute($statement)) {
    $response["success"] = true;
    $response["message"] = "Post saved successfully.";
} else {
    // 쿼리 실행 실패 시
    error_log("Database error: " . mysqli_stmt_error($statement)); // 오류 로그 추가
    $response["success"] = false;
    $response["message"] = "Error: " . mysqli_stmt_error($statement);
}

// 문을 닫고 연결 종료
mysqli_stmt_close($statement);
mysqli_close($con);

echo json_encode($response);
?>
