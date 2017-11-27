'use strict';

describe('Controller Tests', function() {

    describe('Combined Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockPreviousState, MockCombined, MockParticipant, MockTournament, MockUser;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockPreviousState = jasmine.createSpy('MockPreviousState');
            MockCombined = jasmine.createSpy('MockCombined');
            MockParticipant = jasmine.createSpy('MockParticipant');
            MockTournament = jasmine.createSpy('MockTournament');
            MockUser = jasmine.createSpy('MockUser');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity,
                'previousState': MockPreviousState,
                'Combined': MockCombined,
                'Participant': MockParticipant,
                'Tournament': MockTournament,
                'User': MockUser
            };
            createController = function() {
                $injector.get('$controller')("CombinedDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'tournamentControlApp:combinedUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
