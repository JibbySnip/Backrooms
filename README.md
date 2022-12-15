=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=

# CIS 1200 BackroomsGame Project README

### PennKey: liliajb  

=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=  

To run, execute the main method of BackroomsGame.java  

===================  
=: Core Concepts :=  
===================

- List the four core concepts, the features they implement, and why each feature
  is an appropriate use of the concept. Incorporate the feedback you got after
  submitting your proposal.

    1. My first concept is 2D arrays. The game map is stored in a 2d array, which
       holds a static instance of a background tile, or instantiates a version of either
       a door or chest tile. This is because each door or chest tile holds instance-specific
       information, while the other background tiles like wood, stone, and void don't. By
       indexing the 2D array, I can render, do collisions, and whatever else is necessary.
       The 2D array holds objects of type Tile, which is necessary because they hold relevant
       texture data and can respond to player interactions.

    2. I have a novel linked data structure, which is a NullableGraph. Essentially, the
       NullableGraph is a graph with edges that aren't necessarily defined (i.e. the other
       end of the edge could be null). This is useful because in my game, doors don't have
       an endpoint until they're traveled for the first time, so they can either connect to
       a preexisting door, or generate a new room. The rooms are considered vertices, and the
       exits are edges. NullableGraph holds the utilities to link
       null edges together, creating one complete edge. It also holds a collection of all the
       free edges, which is useful for defining new ones.

    3. I have an [approved advanced concept](https://edstem.org/us/courses/25344/discussion/2212605)
       for procedural generation. I used a cellular automata algorithm loosely based on Conway's
       BackroomsGame of Life,
       and drew
       from [this resource](https://www.kodeco.com/2425-procedural-level-generation-in-games-using-a-cellular-automaton-part-1#toc-anchor-007)
       to get an idea of the algorithms I would need to implement. Basically, each level gets a
       random size,
       floor tile (i.e. wood or stone), number of exits, and number of chests. Then, each tile in
       the level
       is randomly assigned to either floor or wall. After that, a series of iterative rules are
       applied
       to each tile based on how many neighbors it has, either leaving a tile the same, or
       transforming it
       into a wall or floor. Then, the largest cave is found, and all others are deleted. Finally,
       exits and
       chests are placed, and the player spawn location is determined.
       More granular details can be found in the code if you're curious.

    4. The project uses inheritance and subtyping for the tiles and the levels. The levels extend
       Vertex in NullableGraph, since in my model, levels are vertices and doors are edges. Further,
       Tile is an interface that has a few utilities for rendering, and TileImpl is an
       implementation
       of that. TileImpl also has a few subclasses, which are door and chest tiles. They're not
       implemented in other files, and are instead created using anonymous classes extending
       TileImpl,
       which are instantiated using a static method in TileImpl. Thus, all tiles can be referred to
       using
       the Tile interface, but when some need additional logic to change their texture after certain
       events
       or any other logic, it can be handled by a subclass.

=========================  
=: Your Implementation :=  
=========================

- Provide an overview of each of the classes in your code, and what their
  function is in the overall game.  
  My classes are as follows:
    - BackroomsGame: the main frame and runnable. Doesn't hold too much besides
      an instance of GamePanel, the instructions button, and the parent frame. It has a main method
      that is
      used to run the game.
    - GamePanel: Extends JComponent, and is nested inside the main frame. It is basically the actual
      game.
      It holds the current level, the player,
      and a graph that is used to represent the level connections. It also has a tick method, which
      it calls 30 times
      per second using a timer. It takes in keyboard input and delegates it to the player and the
      current level. It
      also holds the utilities to render an array of tiles onto the screen with the player centered,
      which it gets from the current level. It also has logic to detect when the game is over, by
      keeping a running
      count of accessible
      shekels, and ending the game when there aren't enough accessible shekels left for the player
      to get through a
      door.
    - InteractResult: This is a record that just holds the outcomes from an interaction with a tile.
      It's really
      useful to have something to consistently pass from a tile to the main game controller with
      useful fields, and
      it means I have to make far fewer subclasses of TileImpl since I have more control over the
      attributes of a tile.
    - Level: This class extends NullableGraph.Vertex, which is useful because I modeled the
      connections between levels
      as edges in a graph. It also holds a 2D array of tiles, which can be used to render the map,
      and can find
      collisions
      between a given bounding box and the tiles that can be collided with in the level. The
      majority of the level,
      though,
      lies in the generation functions as detailed above, which create cave-like structures and
      randomize a bunch of
      attributes in the room.
      It also has a lot of logic intended to prevent "soft-locks", where the player ends up in a
      position
      where there's no new doors to go through and thus cannot make any progress.
    - NullableGraph: This is a novel data structure, which holds a graph that can have edges with
      only one defined
      endpoint. The vertices and edges are defined as static classes within this, which I did
      because I didn't
      feel like giving them their own files. It has a lot of utilities to merge edges with undefined
      endpoints
      , find the edges with undefined endpoints, and pretty much any other utility I thought I would
      need in the game.
    - Player: This is a class that just holds texture and movement logic, as well as a utility to
      get a bounding box.
      It allows the player's texture to be animated and to do a moving animation. It's pretty
      straightforward.
    - Tile: This is an interface for a renderable chunk of a level. It has all the relevant
      utilities defined, and
      is useful for subclassing down the line.
    - TileImpl: This is an implementation of Tile that has most of the things you would actually
      need to make a tile.
      It allows textures to be uploaded, holds collision state, and gives an interaction result. It
      also can return
      subclasses of itself that are either doors or chests, which have slightly more complicated
      logic because their
      textures can change after they're interacted with.
- Were there any significant stumbling blocks while you were implementing your
  game (related to your design, or otherwise)?
    - Soft-locks: It took quite a while to find every condition that would lead the player to be
      stuck in a certain
      area or series of rooms, and prevent that from happening.
    - Rendering: This algorithm was really annoying. Like, it was good, and it worked well, but I
      was just guessing it
      and it took *forever* to find the weird edge cases and issues. I basically implemented a 2D
      game engine rendering
      algorithm from scratch, which was super cool and a little overboard for the assignment. There
      was one incredibly
      frustrating issue that came from issues with rounding and double to int casts. Super annoying
      to figure out.
    - If there's one thing about my design that I really hate, it's that I didn't use the same Point
      class throughout.
      Some parts use Point2D.Double, and other parts use Point. I good reasons for it, but it really
      bothers me.


- Evaluate your design. Is there a good separation of functionality? How well is
  private state encapsulated? What would you refactor, if given the chance?
    - There's one big encapsulation issue where a vertex gets an instance of its parent vertex. I
      don't like that at
      all, but it was the only solution at the time without adding major complexity to the project.
    - I think the functionality is pretty well separated, but it gets blended a bit in GamePanel,
      which holds a lot of
      logic both for rendering and the game state. I think it's appropriate since this game isn't
      that complicated, but
      it's arguably bad design principles.
    - My collision algorithm is really janky, I came up with it on my own and it's not the best.

========================  
=: External Resources :=  
========================

- Cite any external resources (images, tutorials, etc.) that you may have used
  while implementing your game.
    - My friend Parisa drew most of the sprites
    - I
      used [this resource](https://www.kodeco.com/2425-procedural-level-generation-in-games-using-a-cellular-automaton-part-1#toc-anchor-007)
      to learn about the procedural generation algorithm I used. I didn't copy any code from it (
  obviously... it's in
      like c# or smth)
