(function () {
    const autocompleteInput = document.getElementById('main-search-input');
    const searchForm = document.getElementById('main-search-form');
    const cacheKey = 'movieAutocompleteCache';
    const contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
    const fallbackMenu = document.createElement('div');
    let fallbackSuggestions = [];
    let fallbackSelectedIndex = -1;
    let suppressInputHandler = false;
    let lookupTimer = null;

    if (!autocompleteInput || !searchForm) {
        return;
    }

    fallbackMenu.id = 'main-search-fallback-menu';
    fallbackMenu.className = 'autocomplete-suggestions fallback-autocomplete-menu';
    fallbackMenu.style.display = 'none';
    document.body.appendChild(fallbackMenu);

    function getCache() {
        const cachedValue = sessionStorage.getItem(cacheKey);
        if (!cachedValue) {
            return {};
        }
        try {
            return JSON.parse(cachedValue);
        } catch (error) {
            sessionStorage.removeItem(cacheKey);
            return {};
        }
    }

    function setCache(cache) {
        sessionStorage.setItem(cacheKey, JSON.stringify(cache));
    }

    function performNormalSearch() {
        searchForm.requestSubmit();
    }

    function redirectToMovie(movieId) {
        window.location.href = 'single-movie?id=' + encodeURIComponent(movieId);
    }

    function positionFallbackMenu() {
        const rect = autocompleteInput.getBoundingClientRect();
        fallbackMenu.style.left = window.scrollX + rect.left + 'px';
        fallbackMenu.style.top = window.scrollY + rect.bottom + 'px';
        fallbackMenu.style.width = rect.width + 'px';
    }

    function hideFallbackMenu() {
        fallbackMenu.style.display = 'none';
        fallbackMenu.innerHTML = '';
        fallbackSuggestions = [];
        fallbackSelectedIndex = -1;
    }

    function setFallbackSelection(index) {
        const items = fallbackMenu.querySelectorAll('.autocomplete-suggestion');
        items.forEach(function (item) {
            item.classList.remove('autocomplete-selected');
        });

        fallbackSelectedIndex = index;
        if (index >= 0 && index < items.length) {
            items[index].classList.add('autocomplete-selected');
            suppressInputHandler = true;
            autocompleteInput.value = fallbackSuggestions[index].value;
            suppressInputHandler = false;
        }
    }

    function renderFallbackMenu(suggestions) {
        fallbackSuggestions = suggestions;
        fallbackSelectedIndex = -1;
        fallbackMenu.innerHTML = '';

        if (!suggestions.length) {
            hideFallbackMenu();
            return;
        }

        suggestions.forEach(function (suggestion, index) {
            const item = document.createElement('div');
            item.className = 'autocomplete-suggestion';
            item.textContent = suggestion.value;
            item.addEventListener('mousedown', function (event) {
                event.preventDefault();
                autocompleteInput.value = suggestion.value;
                redirectToMovie(suggestion.data.movieId);
            });
            item.addEventListener('mouseenter', function () {
                setFallbackSelection(index);
            });
            fallbackMenu.appendChild(item);
        });

        positionFallbackMenu();
        fallbackMenu.style.display = 'block';
    }

    function moveFallbackSelection(direction) {
        if (!fallbackSuggestions.length) {
            return false;
        }

        let nextIndex = fallbackSelectedIndex + direction;
        if (nextIndex < 0) {
            nextIndex = fallbackSuggestions.length - 1;
        }
        if (nextIndex >= fallbackSuggestions.length) {
            nextIndex = 0;
        }

        setFallbackSelection(nextIndex);
        return true;
    }

    function selectFallbackSuggestion() {
        if (fallbackSelectedIndex < 0 || fallbackSelectedIndex >= fallbackSuggestions.length) {
            return false;
        }

        const suggestion = fallbackSuggestions[fallbackSelectedIndex];
        autocompleteInput.value = suggestion.value;
        redirectToMovie(suggestion.data.movieId);
        return true;
    }

    function fetchSuggestions(query) {
        console.log('autocomplete initiated');

        const cache = getCache();
        if (Object.prototype.hasOwnProperty.call(cache, query)) {
            console.log('using cached result');
            console.log(cache[query]);
            renderFallbackMenu(cache[query]);
            return;
        }

        console.log('sending AJAX request to backend');
        fetch(contextPath + '/movie-suggestion?query=' + encodeURIComponent(query), {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error('Autocomplete request failed with status ' + response.status);
                }
                return response.json();
            })
            .then(function (suggestions) {
                cache[query] = suggestions;
                setCache(cache);
                console.log(suggestions);
                renderFallbackMenu(suggestions);
            })
            .catch(function () {
                hideFallbackMenu();
            });
    }

    autocompleteInput.addEventListener('input', function () {
        if (suppressInputHandler) {
            return;
        }

        const query = autocompleteInput.value.trim();
        if (lookupTimer) {
            window.clearTimeout(lookupTimer);
        }

        if (query.length < 3) {
            hideFallbackMenu();
            return;
        }

        lookupTimer = window.setTimeout(function () {
            fetchSuggestions(query);
        }, 300);
    });

    autocompleteInput.addEventListener('keydown', function (event) {
        if (event.key === 'ArrowDown') {
            if (moveFallbackSelection(1)) {
                event.preventDefault();
            }
            return;
        }

        if (event.key === 'ArrowUp') {
            if (moveFallbackSelection(-1)) {
                event.preventDefault();
            }
            return;
        }

        if (event.key === 'Enter') {
            if (selectFallbackSuggestion()) {
                event.preventDefault();
                return;
            }
            hideFallbackMenu();
            return;
        }

        if (event.key === 'Escape') {
            hideFallbackMenu();
        }
    });

    autocompleteInput.addEventListener('blur', function () {
        window.setTimeout(hideFallbackMenu, 150);
    });

    searchForm.addEventListener('submit', function () {
        autocompleteInput.value = autocompleteInput.value.trim();
    });

    window.addEventListener('resize', positionFallbackMenu);
})();
