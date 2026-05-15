/* Blogplatform frontend logika (jQuery) */
(function ($) {
    'use strict';

    // ---------- Állapot ----------
    const state = {
        token: localStorage.getItem('token') || null,
        username: localStorage.getItem('username') || null,
        userId: parseInt(localStorage.getItem('userId') || '0', 10) || null,
        currentPostId: null,
        page: 0,
        size: 5
    };

    // ---------- Segédfüggvények ----------
    function authHeader() {
        return state.token ? { 'Authorization': 'Bearer ' + state.token } : {};
    }

    function showAlert(message, type) {
        type = type || 'danger';
        const html = `<div class="alert alert-${type} alert-dismissible fade show" role="alert">
            ${$('<div>').text(message).html()}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Bezárás"></button>
        </div>`;
        $('#alertContainer').html(html);
    }

    function escapeHtml(s) {
        return $('<div>').text(s == null ? '' : s).html();
    }

    function formatDate(iso) {
        if (!iso) return '';
        const d = new Date(iso);
        return d.toLocaleString('hu-HU');
    }

    function updateAuthUi() {
        if (state.token) {
            $('.auth-only').removeClass('d-none');
            $('.anon-only').addClass('d-none');
            $('#userBadge').text(state.username);
        } else {
            $('.auth-only').addClass('d-none');
            $('.anon-only').removeClass('d-none');
        }
    }

    function setSession(resp) {
        state.token = resp.token;
        state.username = resp.username;
        state.userId = resp.userId;
        localStorage.setItem('token', resp.token);
        localStorage.setItem('username', resp.username);
        localStorage.setItem('userId', resp.userId);
        updateAuthUi();
    }

    function clearSession() {
        state.token = null;
        state.username = null;
        state.userId = null;
        localStorage.clear();
        updateAuthUi();
    }

    function showView(view) {
        $('[data-view-content]').addClass('d-none');
        $(`[data-view-content="${view}"]`).removeClass('d-none');
        $('#alertContainer').empty();

        if (view === 'list') loadPosts(0);
        if (view === 'new-post') resetPostForm();
    }

    // ---------- API hívások ----------
    function apiCall(method, url, data) {
        return $.ajax({
            method: method,
            url: url,
            data: data ? JSON.stringify(data) : undefined,
            contentType: 'application/json',
            headers: authHeader()
        }).fail(function (xhr) {
            let msg = 'Hiba történt.';
            if (xhr.responseJSON && xhr.responseJSON.message) msg = xhr.responseJSON.message;
            else if (xhr.status === 401) msg = 'Bejelentkezés szükséges.';
            else if (xhr.status === 403) msg = 'Nincs jogosultságod ehhez a művelethez.';
            showAlert(msg, 'danger');
        });
    }

    // ---------- Bejegyzések listázása ----------
    function loadPosts(page) {
        state.page = page;
        apiCall('GET', `/api/posts?page=${page}&size=${state.size}`).done(function (data) {
            renderPostList(data);
            renderPagination(data);
        });
    }

    function renderPostList(pageData) {
        const $list = $('#postList').empty();
        if (!pageData.content || pageData.content.length === 0) {
            $list.html('<div class="col-12 text-center text-muted">Még nincsenek bejegyzések.</div>');
            return;
        }
        pageData.content.forEach(function (p) {
            const card = `
                <div class="col-12 col-md-6 col-lg-4">
                    <div class="card post-card h-100" data-post-id="${p.id}">
                        <div class="card-body">
                            <h5 class="card-title">${escapeHtml(p.title)}</h5>
                            <p class="card-text">${escapeHtml((p.content || '').substring(0, 120))}${p.content.length > 120 ? '…' : ''}</p>
                        </div>
                        <div class="card-footer post-meta">
                            ${escapeHtml(p.authorUsername)} &middot; ${formatDate(p.createdAt)}
                        </div>
                    </div>
                </div>`;
            $list.append(card);
        });
    }

    function renderPagination(pageData) {
        const $p = $('#pagination').empty();
        const total = pageData.totalPages || 1;
        for (let i = 0; i < total; i++) {
            const active = i === pageData.number ? 'active' : '';
            $p.append(`<li class="page-item ${active}"><a class="page-link" href="#" data-page="${i}">${i + 1}</a></li>`);
        }
    }

    // ---------- Bejegyzés részletek ----------
    function openPost(id) {
        state.currentPostId = id;
        apiCall('GET', `/api/posts/${id}`).done(function (p) {
            const owner = state.username && state.username === p.authorUsername;
            const editBtn = owner
                ? `<button class="btn btn-outline-primary btn-sm" id="editPostBtn">Szerkesztés</button>
                   <button class="btn btn-outline-danger btn-sm" id="deletePostBtn">Törlés</button>`
                : '';
            $('#postDetail').html(`
                <h2>${escapeHtml(p.title)}</h2>
                <div class="post-meta mb-3">
                    ${escapeHtml(p.authorUsername)} &middot; ${formatDate(p.createdAt)}
                    ${p.updatedAt && p.updatedAt !== p.createdAt ? '(szerkesztve: ' + formatDate(p.updatedAt) + ')' : ''}
                </div>
                <p style="white-space: pre-wrap;">${escapeHtml(p.content)}</p>
                <div class="d-flex gap-2">${editBtn}</div>
            `);
            showView('detail');
            loadComments(id);
        });
    }

    function loadComments(postId) {
        apiCall('GET', `/api/posts/${postId}/comments`).done(function (list) {
            const $c = $('#commentList').empty();
            if (!list.length) {
                $c.html('<div class="text-muted">Még nincs komment ehhez a bejegyzéshez.</div>');
                return;
            }
            list.forEach(function (c) {
                $c.append(`
                    <div class="comment">
                        <div class="post-meta">${escapeHtml(c.authorUsername)} &middot; ${formatDate(c.createdAt)}</div>
                        <div>${escapeHtml(c.content)}</div>
                    </div>`);
            });
        });
    }

    // ---------- Bejegyzés űrlap ----------
    function resetPostForm() {
        $('#postFormTitle').text('Új bejegyzés');
        $('#postId').val('');
        $('#postTitle').val('');
        $('#postContent').val('');

        showView('new-post');
    }

    function loadPostIntoForm(id) {
        apiCall('GET', `/api/posts/${id}`).done(function (p) {
            $('#postFormTitle').text('Bejegyzés szerkesztése');
            $('#postId').val(p.id);
            $('#postTitle').val(p.title);
            $('#postContent').val(p.content);

            showView('new-post');
        });
    }

    // ---------- Esemény-kötések ----------
    $(document).on('click', '[data-view]', function (e) {
        e.preventDefault();
        const view = $(this).data('view');
        showView(view);
    });

    $(document).on('click', '.post-card', function () {
        openPost($(this).data('post-id'));
    });

    $(document).on('click', '#pagination a', function (e) {
        e.preventDefault();
        loadPosts(parseInt($(this).data('page'), 10));
    });

    $('#loginForm').on('submit', function (e) {
        e.preventDefault();
        apiCall('POST', '/api/auth/login', {
            username: $('#loginUsername').val(),
            password: $('#loginPassword').val()
        }).done(function (resp) {
            setSession(resp);
            showAlert('Sikeres belépés.', 'success');
            showView('list');
        });
    });

    $('#registerForm').on('submit', function (e) {
        e.preventDefault();
        apiCall('POST', '/api/auth/register', {
            username: $('#regUsername').val(),
            email: $('#regEmail').val(),
            password: $('#regPassword').val()
        }).done(function (resp) {
            setSession(resp);
            showAlert('Sikeres regisztráció.', 'success');
            showView('list');
        });
    });

    $('#logoutBtn').on('click', function (e) {
        e.preventDefault();
        clearSession();
        showAlert('Kiléptél.', 'info');
        showView('list');
    });

    $('#postForm').on('submit', function (e) {
        e.preventDefault();
        const id = $('#postId').val();
        const payload = {
            title: $('#postTitle').val(),
            content: $('#postContent').val()
        };
        if (id) {
            apiCall('PUT', `/api/posts/${id}`, payload).done(function () {
                showAlert('Bejegyzés frissítve.', 'success');
                openPost(id);
            });
        } else {
            apiCall('POST', '/api/posts', payload).done(function (p) {
                showAlert('Bejegyzés létrehozva.', 'success');
                openPost(p.id);
            });
        }
    });

    $(document).on('click', '#editPostBtn', function () {
        loadPostIntoForm(state.currentPostId);
    });

    $(document).on('click', '#deletePostBtn', function () {
        if (!confirm('Biztosan törlöd ezt a bejegyzést?')) return;
        apiCall('DELETE', `/api/posts/${state.currentPostId}`).done(function () {
            showAlert('Bejegyzés törölve.', 'success');
            showView('list');
        });
    });

    $('#commentForm').on('submit', function (e) {
        e.preventDefault();
        const content = $('#commentContent').val();
        apiCall('POST', `/api/posts/${state.currentPostId}/comments`, { content: content })
            .done(function () {
                $('#commentContent').val('');
                loadComments(state.currentPostId);
            });
    });

    // ---------- Indulás ----------
    $(function () {
        updateAuthUi();
        showView('list');
    });

})(jQuery);
