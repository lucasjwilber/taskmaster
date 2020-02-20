# taskmaster
This Android app helps users create and keep track of tasks they need to do!

![Home page](./screenshots/ss_homepagev2.PNG)

## Changelog
- 2/11/2020: Created app. Added main activity, Add Task activity, and All Tasks activity, based on wireframes ![here](./assets). Added buttons to start each activity from the home page. Added a button to submit a new task on the Add Task activity, and an onclick function for it with a "Submitted!" animation.

- 2/12/2020: Added a Settings activity and a TaskDetails activity. Added hardcoded task buttons to the homepage, which start the TaskDetails activity, and currently only update the title text on that. Added a gear image button which starts the Settings activity, which contains an input that allows the user to set their username, which is updated on the homepage to replace the "My" in "My Tasks".

- 2/14/2020: Added a new "City" theme to the app, and a theme selector on the Settings activity.

- 2/15/2020: Refactored the theme code. Added XML styles for each theme and removed (most) of the manual view color changes. Removed the action bar.

- 2/18/2020: Implemented a Room database to store Tasks. The RecyclerView on the Main activity now shows all stored tasks, and the Add Task activity can be used to add new Tasks.

- 2/19/2020: Added the Night theme. The recycler view on the Main activity now only shows task state and title, the task body is visible on the Task Details activity.

- 2/20/2020: Refactored app to use AWS Amplify instead of Room for task storage.