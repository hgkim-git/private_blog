import {normalizeSlugInput, slugify} from "/js/utils/slug.js";
import {api} from "/js/utils/api.js";
import {goTo} from "/js/utils/nav.js";

const newTagName = document.getElementById('newTagName');
const newTagSlug = document.getElementById('newTagSlug');
const tagList = document.getElementById('tagList');

newTagName.addEventListener('blur', (e) => {
  if (!newTagSlug.value) {
    newTagSlug.value = slugify(e.target.value, 'tag');
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
  input.value = normalizeSlugInput(input.value, 'tag');
}

// 태그 추가
document.getElementById('addTagForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const name = newTagName.value.trim();
  const slug = newTagSlug.value.trim();

  if (name && slug) {
    try {
      await api.post('/api/tags', {name, slug});
      alert(`태그 "${name}" (${slug})가 추가되었습니다.`);
      goTo({
        cache: false,
      });
    } catch (error) {
      alert(`태그 저장에 실패하였습니다.\n${error.message}`);
    }
  }
});

tagList.addEventListener('click', async (event) => {
  const target = event.target;
  const classList = target.classList;
  if (!classList.contains('tag-action')) {
    return;
  }
  event.stopPropagation();
  const wrapper = target.closest('div.item');
  const id = parseInt(wrapper.dataset.id);
  if (isNaN(id) || id <= 0) {
    console.error('Invalid tag ID:', id);
    return;
  }
  const actionMap = {
    delete: () => deleteTag(id),
    edit: () => showEditForm(id, wrapper),
    save: () => saveTag(id),
    cancel: () => cancelEdit(id, wrapper),
  };
  const actionKey = Object.keys(actionMap).find(key => classList.contains(key));
  if (actionKey) {
    if (actionKey === 'delete' && !confirm(
        '이 태그를 삭제하시겠습니까?\n태그에 연결된 게시글에서 태그가 제거됩니다.')) {
      return;
    }
    await actionMap[actionKey]();
  }
});

// 저장
async function saveTag(id) {
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
    await api.patch(`/api/tags/${id}`, {name, slug});
    alert(`태그 "${name}" (${slug})로 수정되었습니다.`);
    goTo({
      cache: false,
    });
  } catch (error) {
    alert(`태그 수정에 실패했습니다.\n${error.message}`);
  }
}

// 태그 수정
function showEditForm(id, wrapper) {
  wrapper.querySelector('div.item-info').style.display = 'none';
  wrapper.querySelector('div.item-actions').style.display = 'none';
  wrapper.classList.add('editing');
  wrapper.querySelector('div.edit-form').style.display = 'block';
  wrapper.querySelector(`#edit-name-${id}`).focus();
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
}

// 삭제
async function deleteTag(id) {
  try {
    await api.delete(`/api/tags/${id}`);
    alert(`태그가 삭제되었습니다.`);
    goTo({
      cache: false,
    });
  } catch (error) {
    alert(`태그 삭제에 실패했습니다.\n${error.message}`);
  }
}
