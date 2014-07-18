$("#repo-search-box").autocomplete({
    source: "/auto",
    minLength: 3
});

$("#repo-search").submit(function() {
    window.location.href = "/search/" + $("#repo-search-box").val();
    return false;
});