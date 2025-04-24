import requests
from bs4 import BeautifulSoup
import pandas as pd

# 만개의 레시피 사이트의 URL
base_url = "https://www.10000recipe.com/recipe/list.html?q={}"

# 검색할 음식 목록
food_list = ["초밥", "게장", "볶음밥", "갈비", "김치찌개", "된장찌개", "미역국", "오므라이스", "잡채", "케이크", "짜장", "짬뽕",
            "피자", "유자", "건강주스", "김밥", "떡볶이", "두부유부초밥", "샐러드", "라멘", "파스타", "찜닭", "숙주볶음", "마라탕",
            "리조또", "계란빵", "닭가슴살", "냉면", "탕수육", "보쌈", "콩나물국", "술찜", "덮밥", "마제소바", "텐동", "햄버거"]

# 데이터를 저장할 리스트
data = []

# 각 음식별로 한 개의 레시피를 가져오기
for food in food_list:
    url = base_url.format(food)
    
    # 페이지 요청
    response = requests.get(url)
    response.raise_for_status()  # 요청이 성공했는지 확인
    
    # HTML 파싱
    soup = BeautifulSoup(response.text, 'html.parser')
    
    # 첫 번째 레시피 링크 추출
    link = soup.select_one('a.common_sp_link')
    if not link:
        print(f"레시피를 찾을 수 없습니다: {food}")
        continue

    recipe_url = "https://www.10000recipe.com" + link.get('href')
    
    # 레시피 페이지 요청
    recipe_response = requests.get(recipe_url)
    recipe_response.raise_for_status()
    
    # 레시피 페이지 HTML 파싱
    recipe_soup = BeautifulSoup(recipe_response.text, 'html.parser')
    
    # 제목 추출
    title = recipe_soup.select_one('div.view2_summary h3').get_text(strip=True)

    # 레시피 사진 추출
    image = recipe_soup.select_one('div.centeredcrop img')
    image_url = image['src'] if image else '이미지 없음'
    
    # 카테고리 추출
    category_element = recipe_soup.select_one('dl.view2_summary_info1 > dd:nth-of-type(1)')
    category = category_element.get_text(strip=True) if category_element else '카테고리 없음'
    
    # 재료 추출
    ingredients = recipe_soup.select('div.ready_ingre3 ul li')
    ingredients_list = [ingredient.get_text(strip=True) for ingredient in ingredients]
    ingredients_str = ', '.join(ingredients_list)
    
    # 조리 과정 추출
    steps = recipe_soup.select('div.view_step_cont')
    steps_list = [step.get_text(strip=True) for step in steps]
    
    # 데이터 저장
    data.append({
        '레시피 제목': title,
        '이미지 URL': image_url,
        '카테고리': category,
        '재료': ingredients_str,
        '조리 과정': ' '.join(f"{i+1}. {step}" for i, step in enumerate(steps_list))
    })

# DataFrame으로 변환
df = pd.DataFrame(data)

# Excel 파일로 저장
excel_filename = 'recipes.xlsx'
df.to_excel(excel_filename, index=False)

print(f"데이터를 '{excel_filename}' 파일로 저장했습니다.")
