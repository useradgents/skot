# BusinessModel


BusinessModels carry the majority of the application's logic. They are the ones who will call the web service or manage most of the skdata needed in the app.

As always, there is an interface and its implementation in order to keep an abstraction. This allows SKData to be modified only in the BusinessModel to limit misuse.

The business can be linked to a SKState, the state will then be passed as a parameter, and allows access to all the values present in the state, as well as to other BMs in this state or parent states.

It can be seen as a set of use cases linked to the same business, for example, an AccountBM will take care of everything related to the user's account, such as modifying information, passwords, or avatars.