import {slugify} from "/js/utils/slug.js";

// Slug 자동 생성
const newCategoryName = document.getElementById('newCategoryName');
const newCategorySlug = document.getElementById('newCategorySlug');

newCategoryName.addEventListener('input', (e) => {
  if (!newCategorySlug.value) {
    newCategorySlug.value = slugify(e.target.value, 'category');
  }
});

newCategorySlug.addEventListener('input', (e) => {
  e.target.value = e.target.value
  .toLowerCase()
  .replace(/[^a-z0-9-]/g, '')
  .replace(/--+/g, '-')
  .replace(/^-|-$/g, '');
});

// 카테고리 추가
document.getElementById('addCategoryForm').addEventListener('submit', (e) => {
  e.preventDefault();
  const name = newCategoryName.value.trim();
  const slug = newCategorySlug.value.trim();

  if (name && slug) {
    console.log('카테고리 추가:', {name, slug});
    alert(`카테고리 "${name}" (${slug})가 추가되었습니다.`);
    newCategoryName.value = '';
    newCategorySlug.value = '';
  }
});

// 카테고리 수정
function editCategory(id) {
  const item = document.querySelector(`.item[data-id="${id}"]`);
  const nameEl = item.querySelector('.item-name');
  const metaEl = item.querySelector('.item-meta');
  const actionsEl = item.querySelector('.item-actions');

  const currentName = nameEl.textContent;
  const currentSlug = metaEl.textContent.match(/Slug: ([^\s]+)/)[1];

  item.classList.add('editing');

  const editForm = document.createElement('div');
  editForm.className = 'edit-form';
  editForm.innerHTML = `
                <input 
                    type="text" 
                    class="edit-input" 
                    id="editName${id}"
                    value="${currentName}"
                >
                <input 
                    type="text" 
                    class="edit-input" 
                    id="editSlug${id}"
                    value="${currentSlug}"
                    pattern="[a-z0-9-]+"
                >
                <button class="btn btn-success" onclick="saveCategory(${id})">저장</button>
                <button class="btn btn-cancel" onclick="cancelEdit(${id})">취소</button>
            `;

  item.querySelector('.item-info').style.display = 'none';
  actionsEl.style.display = 'none';
  item.insertBefore(editForm, actionsEl);

  // Slug 입력 검증
  document.getElementById(`editSlug${id}`).addEventListener('input', (e) => {
    e.target.value = e.target.value
    .toLowerCase()
    .replace(/[^a-z0-9-]/g, '')
    .replace(/--+/g, '-');
  });
}

// 저장
function saveCategory(id) {
  const name = document.getElementById(`editName${id}`).value.trim();
  const slug = document.getElementById(`editSlug${id}`).value.trim();

  if (!name || !slug) {
    alert('이름과 Slug를 모두 입력해주세요.');
    return;
  }

  if (!/^[a-z0-9-]+$/.test(slug)) {
    alert('Slug는 영문 소문자, 숫자, 하이픈만 사용할 수 있습니다.');
    return;
  }

  console.log('카테고리 수정:', {id, name, slug});
  alert(`카테고리가 "${name}" (${slug})로 수정되었습니다.`);
  location.reload();
}

// 취소
function cancelEdit(id) {
  location.reload();
}

// 삭제
function deleteCategory(id) {
  if (confirm('이 카테고리를 삭제하시겠습니까?\n카테고리에 속한 게시글은 "미분류"로 변경됩니다.')) {
    console.log('카테고리 삭제:', id);
    alert('카테고리가 삭제되었습니다.');
  }
}

// Window에 함수 노출
window.editCategory = editCategory;
window.saveCategory = saveCategory;
window.cancelEdit = cancelEdit;
window.deleteCategory = deleteCategory;