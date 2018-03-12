'use strict';

describe('Controller Tests', function() {

    describe('Game Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockPreviousState, MockGame, MockTournament, MockParticipant, MockGameSet;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockPreviousState = jasmine.createSpy('MockPreviousState');
            MockGame = jasmine.createSpy('MockGame');
            MockTournament = jasmine.createSpy('MockTournament');
            MockParticipant = jasmine.createSpy('MockParticipant');
            MockGameSet = jasmine.createSpy('MockGameSet');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity,
                'previousState': MockPreviousState,
                'Game': MockGame,
                'Tournament': MockTournament,
                'Participant': MockParticipant,
                'GameSet': MockGameSet
            };
            createController = function() {
                $injector.get('$controller')("GameDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'tournamentControlApp:gameUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
