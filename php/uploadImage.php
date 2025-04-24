<?php
$target_dir = "uploads/"; // 파일이 저장될 디렉토리
$uploadOk = 1;
$imageFileType = strtolower(pathinfo($_FILES["file"]["name"], PATHINFO_EXTENSION));

// 이미지 파일 체크 (여기서는 JPEG, PNG, JPG만 허용)
$check = getimagesize($_FILES["file"]["tmp_name"]);
if($check === false) {
    echo json_encode(array("error" => "파일이 이미지가 아닙니다."));
    exit;
}

// 파일 확장자 체크
if(!in_array($imageFileType, array('jpg', 'jpeg', 'png'))) {
    echo json_encode(array("error" => "허용되지 않는 파일 형식입니다."));
    exit;
}

// 파일 이름 중복 방지를 위해 유니크한 이름 생성
$newFileName = uniqid() . '.' . $imageFileType;
$target_file = $target_dir . $newFileName;

// 파일 업로드
if (move_uploaded_file($_FILES["file"]["tmp_name"], $target_file)) {
    // 성공적으로 업로드된 경우 이미지 URL 반환
    $imageUrl = "http://ysrecipe.dothome.co.kr/" . $target_file;
    echo json_encode(array("imageUrl" => $imageUrl));
} else {
    echo json_encode(array("error" => "파일 업로드에 실패했습니다."));
}
?>
