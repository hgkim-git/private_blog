import {normalizeSlugInput, slugify} from "/js/utils/slug.js";
import {api} from "/js/utils/api.js";
import {goTo} from "/js/utils/nav.js";

const newCategoryName = document.getElementById('newCategoryName');
const newCategorySlug = document.getElementById('newCategorySlug');
const categoryList = document.getElementById('categoryList');
const sortable = new Sortable(categoryList, {
  animation: 150,        // 드래그 중 이동 애니메이션 속도 (ms)
  handle: '.drag-handle', // 이 클래스 요소만 드래그 핸들로 사용 (나머지 영역은 드래그 안 됨)
  ghostClass: 'sortable-ghost', // 드래그 중 원래 위치에 표시되는 placeholder의 CSS 클래스
  onEnd: async () => {
    const items = [...categoryList.querySelectorAll('.item')];
    const orders = items.map((el, index) => ({
      id: parseInt(el.dataset.id),
      displayOrder: index,
    }));
    try {
      await api.put('/api/categories/reorder', orders);
    } catch (error) {
      alert(`순서 변경에 실패했습니다.\n${error.message}`);
      goTo({
        cache: false
      });
    }
  },
});

newCategoryName.addEventListener('blur', (e) => {
  if (!newCategorySlug.value) {
    // Slug 자동 생성
    newCategorySlug.value = slugify(e.target.value, 'category');
  }
});

document.querySelectorAll('input.slug-input').forEach(input => {
  ['input', 'compositionend'].forEach(
      event => input.addEventListener(event, handleSlugInput));
});

function handleSlugInput(event) {
  if (event.isComposing) {
    return;
  }
  const input = event.target;
  input.value = normalizeSlugInput(input.value, 'category');
}

// 카테고리 추가
document.getElementById('addCategoryForm').addEventListener('submit',
    async (e) => {
      e.preventDefault();
      const name = newCategoryName.value.trim();
      const slug = newCategorySlug.value.trim();
      if (name && slug) {
        try {
          await api.post('/api/categories', {name, slug});
          alert(`카테고리 "${name}" (${slug})가 추가되었습니다.`);
          goTo({
            cache: false,
          });
        } catch (error) {
          alert(`카테고리 저장에 실패했습니다.\n${error.message}`);
        }
      }
    });

categoryList.addEventListener('click', async (event) => {
  const target = event.target;
  const classList = target.classList;
  if (!classList.contains('category-action')) {
    return;
  }
  event.stopPropagation();
  const wrapper = target.closest('div.item');
  const id = parseInt(wrapper.dataset.id);
  if (isNaN(id) || id <= 0) {
    console.error('Invalid category ID:', id);
    return;
  }
  const actionMap = {
    delete: () => deleteCategory(id),
    edit: () => showEditForm(id, wrapper),
    save: () => saveCategory(id),
    cancel: () => cancelEdit(id, wrapper),
  };
  const actionKey = Object.keys(actionMap).find(key => classList.contains(key));
  if (actionKey) {
    if (actionKey === 'delete' && !confirm('이 카테고리를 삭제하시겠습니까?')) {
      return;
    }
    await actionMap[actionKey]();
  }
});

// 카테고리 수정
function showEditForm(id, wrapper) {
  sortable.option('disabled', true);
  wrapper.querySelector('div.item-info').style.display = 'none';
  wrapper.querySelector('div.item-actions').style.display = 'none';
  wrapper.classList.add('editing');
  wrapper.querySelector('div.edit-form').style.display = 'block';
  wrapper.querySelector(`#edit-name-${id}`).focus();
}

// 저장
async function saveCategory(id) {
  const name = document.getElementById(`edit-name-${id}`).value.trim();
  const slug = document.getElementById(`edit-slug-${id}`).value.trim();

  if (!name || !slug) {
    alert('이름과 Slug를 모두 입력해주세요.');
    return;
  }

  if (!/^[a-z0-9-]+$/.test(slug)) {
    alert('Slug는 영문 소문자, 숫자, 하이픈만 사용할 수 있습니다.');
    return;
  }

  try {
    await api.patch(`/api/categories/${id}`, {name, slug});
    alert(`카테고리가 "${name}" (${slug})로 수정되었습니다.`);
    goTo({
      cache: false,
    });
  } catch (error) {
    alert(`카테고리 수정에 실패했습니다.\n${error.message}`);
  }
}

// 취소
function cancelEdit(id, wrapper) {
  const nameInput = document.getElementById(`edit-name-${id}`);
  nameInput.value = nameInput.dataset.pv;
  const slugInput = document.getElementById(`edit-slug-${id}`);
  slugInput.value = slugInput.dataset.pv;
  wrapper.querySelector('div.item-info').style.display = 'block';
  wrapper.querySelector('div.item-actions').style.display = 'flex';
  wrapper.classList.remove('editing');
  wrapper.querySelector('div.edit-form').style.display = 'none';
  sortable.option('disabled', false);
}

// 삭제
async function deleteCategory(id) {
  try {
    await api.delete(`/api/categories/${id}`);
    alert(`카테고리가 삭제되었습니다.`);
    goTo({
      cache: false,
    });
  } catch (error) {
    alert(`카테고리 삭제에 실패했습니다.\n${error.message}`);
  }
}