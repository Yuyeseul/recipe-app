<?php
// 데이터베이스 연결
$con = mysqli_connect("", "", "", "");
mysqli_query($con, 'SET NAMES utf8');

// POST로부터 데이터 수신
$id = isset($_POST["id"]) ? $_POST["id"] : "";
$newNickname = isset($_POST["nickname"]) ? $_POST["nickname"] : "";
$oldNickname = isset($_POST["old_nickname"]) ? $_POST["old_nickname"] : ""; // 이전 닉네임 추가

// user 테이블에서 닉네임 업데이트 (아이디와 닉네임이 일치할 때)
$updateUserStmt = mysqli_prepare($con, "UPDATE user SET nickname = ? WHERE id = ? AND nickname = ?");
mysqli_stmt_bind_param($updateUserStmt, "sss", $newNickname, $id, $oldNickname);
$resultUser = mysqli_stmt_execute($updateUserStmt);

// orders 및 comments 테이블에서 닉네임 업데이트 (이전 닉네임이 일치할 때)
$updatebookmarkStmt = mysqli_prepare($con, "UPDATE bookmark SET nickname = ? WHERE nickname = ?");
mysqli_stmt_bind_param($updatebookmarkStmt, "ss", $newNickname, $oldNickname);
$resultbookmark = mysqli_stmt_execute($updatebookmarkStmt);

$updatefavoriteStmt = mysqli_prepare($con, "UPDATE favorite SET nickname = ? WHERE nickname = ?");
mysqli_stmt_bind_param($updatefavoriteStmt, "ss", $newNickname, $oldNickname);
$resultfavorite = mysqli_stmt_execute($updatefavoriteStmt);

$updaterecipeStmt = mysqli_prepare($con, "UPDATE recipe SET nickname = ? WHERE nickname = ?");
mysqli_stmt_bind_param($updaterecipeStmt, "ss", $newNickname, $oldNickname);
$resultrecipe = mysqli_stmt_execute($updaterecipeStmt);

$updaterefrigeratorStmt = mysqli_prepare($con, "UPDATE refrigerator SET nickname = ? WHERE nickname = ?");
mysqli_stmt_bind_param($updaterefrigeratorStmt, "ss", $newNickname, $oldNickname);
$resultrefrigerator = mysqli_stmt_execute($updaterefrigeratorStmt);

$updateshoppingStmt = mysqli_prepare($con, "UPDATE shopping SET nickname = ? WHERE nickname = ?");
mysqli_stmt_bind_param($updateshoppingStmt, "ss", $newNickname, $oldNickname);
$resultshopping = mysqli_stmt_execute($updateshoppingStmt);


// 응답 배열 초기화
$response = array();

// 쿼리 실행 결과에 따른 응답
if ($resultUser && $resultbookmark && $resultfavorite && $resultrecipe && $resultrefrigerator && $resultshopping) {
    $response["success"] = true; // 업데이트 성공
} else {
    $response["success"] = false; // 업데이트 실패
}

// JSON 형식으로 응답 출력
echo json_encode($response);

// 데이터베이스 연결 종료
mysqli_stmt_close($updateUserStmt);
mysqli_stmt_close($updatebookmarkStmt);
mysqli_stmt_close($updatefavoriteStmt);
mysqli_stmt_close($updaterecipeStmt);
mysqli_stmt_close($updaterefrigeratorStmt);
mysqli_stmt_close($updateshoppingStmt);
mysqli_close($con);
?>
