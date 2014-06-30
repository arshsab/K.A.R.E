(function (window, document, undefined) {
    $(function() {
        /**
         * This data must be formatted as such:
         * [ {x: 1, y: 20}, {x: 25, y: 33}]
         */
        var updateCycle = $.parseJSON($("#update-cycle-time").data("time"));
        $('#update-cycle-time').epoch({
            type: 'area',
            data: updateCycle,
            axes: ['left', 'bottom']
        });

        var $repoTime = $("#repos-time");
        var reposTimeInfo = $.parseJSON($repoTime.data("info")),
            starsUpdated  = $.parseJSON($repoTime.data("stars")),
            correlations  = $.parseJSON($repoTime.data("correlations"));
        var repoTimeData = [
            {
                label: 'Repo Info Updated', 
                values: reposTimeInfo
            },
            {
                label: 'Stars Updated',
                values: starsUpdated
            },
            {
                label: 'Correlations Updated',
                values: correlations
            }

        ];
       $repoTime.epoch({
           type: 'area',
           data: repoTimeData,
           axes: ['left', 'bottom']
       });
    });
})(window, document);
