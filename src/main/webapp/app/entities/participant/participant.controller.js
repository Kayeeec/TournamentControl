(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('ParticipantController', ParticipantController);

    ParticipantController.$inject = ['Participant'];

    function ParticipantController(Participant) {

        var vm = this;

        vm.participants = [];

        loadAll();

        function loadAll() {
            Participant.query(function(result) {
                vm.participants = result;
                vm.searchQuery = null;
            });
        }
    }
})();
