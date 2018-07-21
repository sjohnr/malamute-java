<?php

$globals = array();

/**
 * Read a file into a string.
 *
 * @param $filename string The file name to read
 * @return string The file contents
 */
function read_file($filename) {
    $handle = fopen($filename, 'r');
    $contents = fread($handle, filesize($filename));
    fclose($handle);

    return $contents;
}

/**
 * Parse an XML file.
 *
 * @param $xml string The xml string to parse
 * @return SimpleXMLElement The parsed xml result
 */
function parse_xml($xml) {
    return simplexml_load_string($xml);
}

/**
 * Read an XML file given as an argument.
 *
 * @param $filename string The file name to read
 * @return SimpleXMLElement The parsed XML read from a file
 */
function read_xml($filename) {
    global $globals;
    $contents = read_file($filename);

    $xml = parse_xml($contents);
    $globals['relative_path'] = dirname($filename);
    $globals['xml'] = $xml;

    return $xml;
}

/**
 * @param $filename
 */
function output($filename) {
    ob_get_clean();

    global $globals;
    $globals['output_filename'] = $filename;

    echo "Generating $filename...\n";
    ob_start("write_output");
}

/**
 *
 */
function done() {
    ob_get_clean();
}

/**
 * @param $output
 */
function write_output($output) {
    global $globals;
    $filename = $globals['output_filename'];

    $handle = fopen($filename, 'w+');
    fwrite($handle, $output);
    fclose($handle);
}

/**
 * @param $class
 */
function resolve_includes($class) {
    global $globals;
    
    $includes = $class->xpath('include');
    if (count($includes) == 0) {
        return;
    }
    
    $relative_path = '.';
    if ($globals['relative_path']) {
        $relative_path = $globals['relative_path'];
    }
    
    foreach ($includes as $include) {
        $filename = $include['filename'];
        if ($filename) {
            if (!strstr($filename, '/')) {
                $filename = $relative_path . '/' . $filename;
            }
            
            if (file_exists($filename)) {
                $content = "<root>" . read_file($filename) . "</root>";
                $parsed = parse_xml($content);
                foreach ($parsed->children() as $child) {
                    $class[$child->getName()] = (string) $child;
                }
            } else {
                echo "E: could not find file ${filename}";
            }
        } else {
            echo "E: required attribute 'filename' not defined\n";
        }
    }
}

/**
 * @param $path
 */
function create_directories($path) {
    if (!is_dir("../src/main/java/$path")) {
        mkdir("../src/main/java/$path", 0755, true);
    }
}

/**
 * @param $class
 * @param $state
 * @return array
 */
function resolve_events($class, $state) {
    $events = array();
    foreach ($state->event as $event) {
        $events[(string) $event['name']] = $event;
    }

    if ($state['inherit']) {
        $found = false;
        foreach ($class->state as $superstate) {
            if ((string) $superstate['name'] == (string) $state['inherit']) {
                $found = true;
                foreach ($superstate->event as $event) {
                    $name = (string)$event['name'];
                    if (!$events[$name]) {
                        $events[$name] = $event;
                    }
                }
                break;
            }
        }

        if (!$found) {
            echo "E: superstate ${state['inherit']} isn't defined\n";
        }
    }

    return array_values($events);
}

/**
 * @param $class
 * @return array
 */
function get_all_states($class) {
    $states = array();
    foreach ($class->state as $state) {
        $found = false;
        foreach ($class->state as $child) {
            if ((string) $state['name'] == (string) $child['inherit']) {
                $found = true;
                break;
            }
        }

        // Exclude super states from list. Super states are states whose names
        // are found in an 'inherit' attribute.
        if (!$found) {
            array_push($states, $state);
        }
    }

    return $states;
}

/**
 * @param $class
 * @return array
 */
function get_all_events($class) {
    $events = array();
    foreach ($class->state as $state) {
        foreach ($state->event as $event) if ((string) $event['name'] != '*') {
            if (array_search((string) $event['name'], $events) === false) {
                array_push($events, $event['name']);
            }
        }
    }

    return $events;
}

/**
 * @param $class
 * @return array
 */
function get_all_actions($class) {
    $actions = array();
    foreach ($class->state as $state) {
        foreach ($state->event as $event) {
            foreach ($event->action as $action) {
                $name = (string)$action['name'];
                if ($name != 'send' && $name != 'recv' && !array_search($name, $actions)) {
                    array_push($actions, $name);
                }
            }
        }
    }

    return $actions;
}

/**
 * @param $proto
 * @return array
 */
function get_messages_by_name($proto) {
    $messages = array();
    foreach ($proto->message as $message) {
        $messages[(string) $message['name']] = $message;
    }

    return $messages;
}

/**
 * @param $class
 * @param $messages
 * @return array
 */
function get_all_fields_by_name($class, $messages) {
    $fields = array();
    foreach ($class->recv->message as $method) {
        $message = $messages[(string)$method['name']];
        foreach ($message->field as $field) {
            $fields[(string) $field['name']] = $field;
        }
    }

    foreach ($class->reply as $reply) {
        foreach ($reply->field as $field) {
            $fields[(string) $field['name']] = $field;
        }
    }

    return $fields;
}

/**
 * @param $class
 * @return array
 */
function get_replies_by_name($class) {
    $replies = array();
    foreach ($class->reply as $reply) {
        $replies[(string) $reply['name']] = $reply;
    }
    
    return $replies;
}

/**
 * @param $field
 * @return string
 */
function get_parameter_type($field) {
    $type = (string) $field['type'];
    $size = (string) $field['size'];
    $result = 'void';
    if ($type == 'string') {
        $result = 'String';
    } else if ($type == 'integer') {
        $result = 'Integer';
    } else if ($type == 'frame') {
        $result = 'Frame';
    } else if ($type == 'msg' || $type == 'frames') {
        $result = 'Message';
    } else if ($type == 'number' && $size == '1') {
        $result = 'Byte';
    } else if ($type == 'number' && $size == '2') {
        $result = 'Short';
    } else if ($type == 'number' && $size == '4') {
        $result = 'Integer';
    } else if ($type == 'number' && $size == '8') {
        $result = 'Long';
    }
    
    return $result;
}

/**
 * @param $field
 * @return string
 */
function get_pushpop_method($field) {
    $type = (string) $field['type'];
    $size = (string) $field['size'];
    if ($type == 'string') {
        $result = 'String';
    } else if ($type == 'integer') {
        $result = 'Int';
    } else if ($type == 'frame') {
        $result = 'Frame';
    } else if ($type == 'msg' || $type == 'frames') {
        $result = 'Frames';
    } else if ($type == 'number' && $size == '1') {
        $result = 'Byte';
    } else if ($type == 'number' && $size == '2') {
        $result = 'Short';
    } else if ($type == 'number' && $size == '4') {
        $result = 'Int';
    } else if ($type == 'number' && $size == '8') {
        $result = 'Long';
    } else {
        throw new RuntimeException("Invalid type $type");
    }
    
    return $result;
}

/**
 * @param $xmlarray
 * @return array
 */
function array_of($xmlarray) {
    $array = array();
    foreach ($xmlarray as $item) {
        array_push($array, $item);
    }

    return $array;
}

/**
 * @param $arr
 * @param $index
 * @return bool
 */
function last($arr, $index) {
    return $index + 1 === count($arr);
}

/**
 * @param $arr
 * @param $index
 * @return bool
 */
function first($index) {
    return $index <= 0;
}

/**
 * @param $content
 * @param int $spaces
 * @return string
 */
function block_comment($content, $spaces = 0) {
    $delim = "\n" . str_repeat(' ', $spaces) . ' * ';
    $comment = '';
    foreach (explode("\n", trim($content)) as $i => $line) {
        if ($i > 0) {
            $comment .= $delim;
        }
        $comment .= trim($line);
    }

    return $comment;
}

/**
 * @param $string
 * @return string
 */
function nl($string = '') {
    return "$string\n";
}

/**
 * @param $string
 * @return string
 */
function package($string) {
    return str_replace('/', '.', $string);
}

/**
 * @param $string
 * @return string
 */
function cvar($string) {
    return strtolower(str_replace(' ', '_', sanitize($string)));
}

/**
 * @param $string
 * @return string
 */
function cconst($string) {
    return strtoupper(str_replace(' ', '_', sanitize($string)));
}

/**
 * @param $string
 * @return string
 */
function jclass($string) {
    return ucfirst(str_replace(' ', '', ucwords(strtolower(sanitize($string)))));
}

/**
 * @param $string
 * @return string
 */
function jvar($string) {
    return lcfirst(str_replace(' ', '', ucwords(strtolower(sanitize($string)))));
}

/**
 * @param $string
 * @return string
 */
function ccomment($string) {
    return ucfirst(strtolower(sanitize($string))) . '.';
}

/**
 * @param $string
 * @return string
 */
function sanitize($string) {
    if (!preg_match('/[a-zA-Z]/', $string[0])) {
        $string = '_' . $string;
    }

    return str_replace(str_split(',<.>/?;:\'"[{]}\\|`~!@#$%^&*()-_+='), ' ', $string);
}
