<?php
// 데이터베이스 연결 정보 설정
$con = mysqli_connect("", "", "", "");
mysqli_query($con, 'SET NAMES utf8');

// 연결 확인
if (mysqli_connect_errno()) {
    die("Database connection failed: " . mysqli_connect_error());
}

// POST 요청에서 필요한 데이터 가져오기
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $recipeName = $_POST['recipeName'];
    $newFavoriteCount = $_POST['favorite'];

    // 레시피 이름과 좋아요 수가 제공되었는지 확인
    if (!empty($recipeName) && is_numeric($newFavoriteCount)) {
        
        // SQL 업데이트 쿼리 생성 및 준비
        $sql = "UPDATE recipe SET favorite = ? WHERE recipeName = ?";
        $stmt = mysqli_prepare($con, $sql);

        // 쿼리 실행 준비
        if ($stmt) {
            // 쿼리에 변수 바인딩
            mysqli_stmt_bind_param($stmt, "is", $newFavoriteCount, $recipeName);

            // 쿼리 실행
            if (mysqli_stmt_execute($stmt)) {
                echo json_encode(array("status" => "success", "message" => "Favorite count updated successfully."));
            } else {
                echo json_encode(array("status" => "error", "message" => "Failed to update favorite count."));
            }
            // 쿼리 리소스 해제
            mysqli_stmt_close($stmt);
        } else {
            echo json_encode(array("status" => "error", "message" => "Failed to prepare SQL statement."));
        }
    } else {
        echo json_encode(array("status" => "error", "message" => "Invalid input."));
    }
}

// 데이터베이스 연결 해제
mysqli_close($con);
?>
