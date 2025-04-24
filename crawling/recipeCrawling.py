import requests
from bs4 import BeautifulSoup
import pandas as pd
import time

# 만개의 레시피 사이트의 URL (페이지 넘버 포함)
base_url = "https://www.10000recipe.com/recipe/list.html?order=reco&page={}"

# 데이터를 저장할 리스트
data = []

# 200개 정도의 레시피를 가져오기 위해 페이지 수를 조정
for page in range(1, 3):  # 2페이지까지만 크롤링
    url = base_url.format(page)

    # 페이지 요청 및 예외 처리
    max_retries = 3  # 최대 재시도 횟수
    for _ in range(max_retries):
        try:
            response = requests.get(url)
            response.raise_for_status()  # 요청이 성공했는지 확인
            break  # 성공하면 반복 종료
        except requests.exceptions.RequestException as e:
            print(f"요청 실패: {e}")
            time.sleep(1)  # 잠시 대기 후 재시도

    # HTML 파싱
    soup = BeautifulSoup(response.text, 'html.parser')

    # 각 페이지에서 레시피 목록 추출
    recipes = soup.select('li.common_sp_list_li')

    for recipe in recipes:
        # 레시피 링크 추출
        link = recipe.select_one('a.common_sp_link')
        if not link:
            continue

        recipe_url = "https://www.10000recipe.com" + link.get('href')

        # 레시피 페이지 요청 및 예외 처리
        for _ in range(max_retries):
            try:
                recipe_response = requests.get(recipe_url)
                recipe_response.raise_for_status()
                break
            except requests.exceptions.RequestException as e:
                print(f"레시피 요청 실패: {e}")
                time.sleep(1)

        # 레시피 페이지 HTML 파싱
        recipe_soup = BeautifulSoup(recipe_response.text, 'html.parser')

        # 제목 추출
        title_element = recipe_soup.select_one('div.view2_summary h3')
        title = title_element.get_text(strip=True) if title_element else '제목 없음'

        # 레시피 사진 추출
        image = recipe_soup.select_one('div.centeredcrop img')
        image_url = image['src'] if image else '이미지 없음'
        
        # 카테고리 추출
        category_element = recipe_soup.select_one('dl.view2_summary_info1 > dd:nth-of-type(1)')
        category = category_element.get_text(strip=True) if category_element else '카테고리 없음'

        # 재료 추출
        ingredients = recipe_soup.select('div.ready_ingre3 ul li')
        ingredients_list = []
        for ingredient in ingredients:
            ingredient_text = ingredient.get_text(strip=True)
            parts = ingredient_text.split(maxsplit=1)
            if len(parts) == 2:
                amount, name = parts
                ingredients_list.append({'갯수': amount, '재료명': name})
            else:
                ingredients_list.append({'갯수': '알 수 없음', '재료명': ingredient_text})

        # 재료 리스트를 문자열로 변환
        ingredients_str = ', '.join([f"{item['갯수']} {item['재료명']}" for item in ingredients_list]) if ingredients_list else '재료 없음'

        # 조리 과정 추출
        steps = recipe_soup.select('div.view_step_cont')
        steps_list = [step.get_text(strip=True) for step in steps]

        # 데이터 저장
        data.append({
            '레시피 제목': title,
            '이미지 URL': image_url,
            '카테고리': category,
            '재료': ingredients_str,
            '조리 과정': ' '.join(f"{i+1}. {step}" for i, step in enumerate(steps_list)) if steps_list else '조리 과정 없음'
        })

    # 페이지 요청 후 잠시 대기
    time.sleep(1)  # 1초 대기

# DataFrame으로 변환
df = pd.DataFrame(data)

# Excel 파일로 저장
excel_filename = '1.xlsx'
df.to_excel(excel_filename, index=False)

print(f"데이터를 '{excel_filename}' 파일로 저장했습니다.")
