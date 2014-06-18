var repos = new Bloodhound({
    datumTokenizer: function (d) {
        return Bloodhound.tokenizers.whitespace(d.value);
    },
    queryTokenizer: Bloodhound.tokenizers.whitespace,
    remote: {
        url: 'http://kare.progger.io/auto?search=%QUERY',
        filter: function (repoList) {
            return $.map(repoList.results, function (repo) {
                return {
                    value: repo.name
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
