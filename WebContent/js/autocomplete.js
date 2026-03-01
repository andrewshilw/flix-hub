(function () {
    var autocompleteInput = $("#autocomplete");

    if (autocompleteInput.length === 0) {
        return;
    }

    var contextPath = autocompleteInput.data("context-path") || "";
    var cacheKey = "fablixAutocompleteCache";

    function readCache() {
        try {
            return JSON.parse(sessionStorage.getItem(cacheKey) || "{}");
        } catch (error) {
            return {};
        }
    }

    function writeCache(cache) {
        sessionStorage.setItem(cacheKey, JSON.stringify(cache));
    }

    function useSuggestions(query, suggestions, doneCallback) {
        console.log("autocomplete used suggestion list", suggestions);
        doneCallback({ suggestions: suggestions });
    }

    function handleLookup(query, doneCallback) {
        var trimmedQuery = $.trim(query);
        var cache = readCache();

        console.log("autocomplete initiated");

        if (Object.prototype.hasOwnProperty.call(cache, trimmedQuery)) {
            console.log("autocomplete using cached results");
            useSuggestions(trimmedQuery, cache[trimmedQuery], doneCallback);
            return;
        }

        console.log("autocomplete sending ajax request to server");

        $.ajax({
            method: "GET",
            url: contextPath + "/movie-suggestion",
            data: { query: trimmedQuery },
            dataType: "json",
            success: function (data) {
                cache[trimmedQuery] = data;
                writeCache(cache);
                useSuggestions(trimmedQuery, data, doneCallback);
            },
            error: function () {
                useSuggestions(trimmedQuery, [], doneCallback);
            }
        });
    }

    function handleSelectSuggestion(suggestion) {
        window.location.href = contextPath + "/single-movie?id=" + encodeURIComponent(suggestion.data.movieId);
    }

    autocompleteInput.autocomplete({
        lookup: function (query, doneCallback) {
            handleLookup(query, doneCallback);
        },
        onSelect: function (suggestion) {
            handleSelectSuggestion(suggestion);
        },
        deferRequestBy: 300,
        minChars: 3
    });
})();
