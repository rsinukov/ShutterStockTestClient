# Test client app for ShutterStock search

## Description of the problem and solution.

Task is to implement an app that fetches pictures from the Shutterstock API (http://api.shutterstock.com/)
and displays them in an infinite scrollable view.
The API documentation is here: `https://developers.shutterstock.com/api/v2`.
App uses the `search` endpoint of Shutterstock, making sure that new pictures
will be fetched and shown when the user scrolls to the end of the list.

Solution is based on Clean Architecture concept and `MVI` pattern for UI architecture

### Main entities:
* `View` - entry point. Has one interesting method `render` that accepts view model and binds it to controls.
Handles lifecycle and subscriptions
* `Presenter` - maps business entities to view entities. Also has state reducer.
Handles state restoration by saving it in `reduce` function
* `Interactor` - connects view to business logic. Contains inside required repositories and use cases.
Knows how to connect them to fulfill screen requirements
* `Repository` - stores data
* `UseCase` - one piece of business task (ex, load new page from api and save it to repo)
* `API` - wrapper on top of REST API

### Important Helpers:
* `AdapterDelegate` - base class for adapter with view holder delegates
* `OneShot` and `ViewStateWithId` - helper to put one time actions to view state (ex, show toast on error)
* `ShutterStockDagger` - dagger components holder

## The reasoning behind your technical choices, including architectural.
In my work experience I found that `MVI` is most extensible UI layer pattern.
It gives easy state restoration, independent and reusable building blocks, testability, unidirectional data flow and easy debugging.
Using Kotlin, Dagger and RxJava is must-have for nowadays Android development.

## Trade-offs you might have made, anything you left out, or what you might do differently if you were to spend additional time on the project.
* More test cases inside `SearchIntegrationTest` (mentioned in TODO inside the file).
* Better manual testing to verify that implementation works as required.
* CI integration

## Link to your resume or public profile.
https://www.linkedin.com/in/rustam-sinukov-397366100/
