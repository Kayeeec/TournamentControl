(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('swiss', {
            parent: 'entity',
            url: '/swiss?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'tournamentControlApp.swiss.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/swiss/swisses.html',
                    controller: 'SwissController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'id,asc',
                    squash: true
                },
                search: null
            },
            resolve: {
                pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                    return {
                        page: PaginationUtil.parsePage($stateParams.page),
                        sort: $stateParams.sort,
                        predicate: PaginationUtil.parsePredicate($stateParams.sort),
                        ascending: PaginationUtil.parseAscending($stateParams.sort),
                        search: $stateParams.search
                    };
                }],
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('swiss');
                    $translatePartialLoader.addPart('global');
                    $translatePartialLoader.addPart('tournament');
                    $translatePartialLoader.addPart('setSettings');
                    return $translate.refresh();
                }]
            }
        })
        .state('swiss-detail', {
            parent: 'swiss',
            url: '/swiss/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'tournamentControlApp.swiss.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/swiss/swiss-detail.html',
                    controller: 'SwissDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('swiss');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Swiss', function($stateParams, Swiss) {
                    return Swiss.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'swiss',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('swiss-detail.edit', {
            parent: 'swiss-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/swiss/swiss-dialog.html',
                    controller: 'SwissDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Swiss', function(Swiss) {
                            return Swiss.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('swiss.new', {
            parent: 'swiss',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/swiss/swiss-dialog.html',
                    controller: 'SwissDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                name: null,
                                note: null,
                                pointsForWinning: null,
                                pointsForLosing: null,
                                pointsForTie: null,
                                created: null,
                                numberOfSets: null,
                                setsToWin: null,
                                tiesAllowed: null,
                                playingFields: null,
                                
                                rounds: null,
                                roundsToGenerate: null,
                                color: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('swiss', null, { reload: 'swiss' });
                }, function() {
                    $state.go('swiss');
                });
            }]
        })
        .state('swiss.edit', {
            parent: 'swiss',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/swiss/swiss-dialog.html',
                    controller: 'SwissDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Swiss', function(Swiss) {
                            return Swiss.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('swiss', null, { reload: 'swiss' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('swiss.delete', {
            parent: 'swiss',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/swiss/swiss-delete-dialog.html',
                    controller: 'SwissDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Swiss', function(Swiss) {
                            return Swiss.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('swiss', null, { reload: 'swiss' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
