import {api} from "/js/utils/api.js";
import {goTo} from "/js/utils/nav.js";

const selectAllCheckbox = document.getElementById('selectAll');
const rowCheckboxes = [...document.querySelectorAll('.row-checkbox')];
const selectedCount = document.getElementById('selectedCount');
const bulkDeleteBtn = document.getElementById('bulkDeleteBtn');

const getChecked = () => rowCheckboxes.filter(cb => cb.checked);

selectAllCheckbox.addEventListener('change', () => {
  rowCheckboxes.forEach(cb => {
    cb.checked = selectAllCheckbox.checked;
  });
  updateSelectedCount();
});

// 이벤트 위임: tbody에 하나의 리스너로 모든 행 체크박스 처리
document.querySelector('tbody').addEventListener('change', (e) => {
  if (e.target.classList.contains('row-checkbox')) {
    updateSelectAllState();
    updateSelectedCount();
  }
});

function updateSelectAllState() {
  const checkedCount = getChecked().length;
  selectAllCheckbox.checked = checkedCount === rowCheckboxes.length;
  // checkbox에 [-] 표시
  selectAllCheckbox.indeterminate = checkedCount > 0 && checkedCount
      < rowCheckboxes.length;
}

function updateSelectedCount() {
  const checkedCount = getChecked().length;
  if (checkedCount > 0) {
    selectedCount.textContent = `${checkedCount}개 선택됨`;
    bulkDeleteBtn.style.display = 'block';
  } else {
    selectedCount.textContent = '';
    bulkDeleteBtn.style.display = 'none';
  }
}

// 일괄 삭제
bulkDeleteBtn.addEventListener('click', async () => {
  const selectedIds = getChecked()
  .map(cb => parseInt(cb.closest('tr').dataset.id))
  .filter(id => !isNaN(id) && id > 0);

  if (!confirm(`선택한 ${selectedIds.length}개의 게시글을 삭제하시겠습니까?`)) {
    return;
  }

  let failed = 0;
  await Promise.all(selectedIds.map(async id => {
    try {
      await api.delete(`/api/posts/${id}`);
    } catch (error) {
      console.error('게시글 삭제 중 오류 발생:', error);
      failed++;
    }
  }));

  if (failed > 0) {
    alert(`${failed}개의 게시글 삭제 중 오류가 발생했습니다.`);
  }
  goTo(window.location.href, {
    cache: false,
  });
});

// 개별 삭제
document.querySelectorAll('.action-btn').forEach(btn => {
  btn.addEventListener('click', async (e) => {
    const postId = parseInt(btn.closest('tr').dataset.id);
    if (isNaN(postId) || postId <= 0) {
      console.error('Invalid post ID:', postId);
      return;
    }
    if (e.target.classList.contains('delete') && confirm('이 게시글을 삭제하시겠습니까?')) {
      await deletePost(postId);
    } else {
      editPost(postId);
    }
  });
});

async function deletePost(postId) {
  try {
    await api.delete('/api/posts/' + postId);
    alert('게시글이 삭제되었습니다.');
    goTo({
      cache: false,
    });
  } catch (error) {
    console.error('게시글 삭제 중 오류 발생:', error);
    alert('게시글 삭제 중 오류가 발생했습니다. 다시 시도해주세요.');
  }
}

// 수정
function editPost(id) {
  goTo(`/admin/posts/form?id=${id}`, {
    cache: false,
  });
}

// 필터 초기화
const resetBtn = document.getElementById('resetBtn');
const searchBtn = document.getElementById('searchBtn');
const searchInput = document.getElementById('searchInput');
const categoryFilter = document.getElementById('categoryFilter');
const statusFilter = document.getElementById('statusFilter');

// 검색 조건 유지
(() => {
  const url = new URL(window.location.href);
  url.searchParams.forEach((value, key) => {
    switch (key) {
      case 'keyword':
        searchInput.value = value;
        break;
      case 'category':
        categoryFilter.value = value;
        break;
      case 'status':
        statusFilter.value = value;
        break;
    }
  });
})();

resetBtn.addEventListener('click', () => {
  searchInput.value = '';
  categoryFilter.value = '';
  statusFilter.value = '';
  search();
});

searchInput.addEventListener('keyup', (e) => {
  if (e.key === 'Enter') {
    e.preventDefault();
    e.stopPropagation();
    search();
  }
});

function search() {
  const options = {
    cache: false,
    clearParams: true,
  };
  const params = {};
  const searchTermsMap = {
    keyword: searchInput.value.trim(),
    category: categoryFilter.value.trim(),
    status: statusFilter.value.trim(),
  };
  for (const key in searchTermsMap) {
    const value = searchTermsMap[key];
    if (value) {
      params[key] = value;
    }
  }
  Object.assign(options, {params});
  goTo(options);
}

searchBtn.addEventListener('click', search);