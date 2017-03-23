'use strict';

describe('Controller Tests', function() {

    describe('Tournament Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockPreviousState, MockTournament, MockGame, MockUser, MockParticipant;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockPreviousState = jasmine.createSpy('MockPreviousState');
            MockTournament = jasmine.createSpy('MockTournament');
            MockGame = jasmine.createSpy('MockGame');
            MockUser = jasmine.createSpy('MockUser');
            MockParticipant = jasmine.createSpy('MockParticipant');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity,
                'previousState': MockPreviousState,
                'Tournament': MockTournament,
                'Game': MockGame,
                'User': MockUser,
                'Participant': MockParticipant
            };
            createController = function() {
                $injector.get('$controller')("TournamentDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'tournamentControlApp:tournamentUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
