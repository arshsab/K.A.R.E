var repos = new Bloodhound({
    datumTokenizer: function (d) {
        return Bloodhound.tokenizers.whitespace(d.value);
    },
    queryTokenizer: Bloodhound.tokenizers.whitespace,
    remote: {
        url: '/auto/%QUERY',
        filter: function (repoList) {
            return $.map(repoList.results, function (repo) {
                return {
                    value: repo
                };
            });
        }
    }
});

repos.initialize();

$('#search-box').typeahead(null, {
    displayKey: 'value',
    source: repos.ttAdapter()
});

$("#results-form").submit(function() {
    var repo = $('#search-box').val();
    window.location.href = '/search/' + repo;
    return false;
});
